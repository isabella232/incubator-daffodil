/* Copyright (c) 2016 Tresys Technology, LLC. All rights reserved.
 *
 * Developed by: Tresys Technology, LLC
 *               http://www.tresys.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal with
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the names of Tresys Technology, nor the names of its contributors
 *     may be used to endorse or promote products derived from this Software
 *     without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 * SOFTWARE.
 */

package edu.illinois.ncsa.daffodil.processors.unparsers

import org.junit.Test
import org.junit.Assert._
import edu.illinois.ncsa.daffodil.xml._
import edu.illinois.ncsa.daffodil.Implicits._
import edu.illinois.ncsa.daffodil.equality._
import edu.illinois.ncsa.daffodil.util.TestUtils
import edu.illinois.ncsa.daffodil.util.SchemaUtils
import edu.illinois.ncsa.daffodil.processors.DataProcessor
import edu.illinois.ncsa.daffodil.compiler.Compiler
import edu.illinois.ncsa.daffodil.processors.DISimple
import edu.illinois.ncsa.daffodil.processors.DIComplex
import edu.illinois.ncsa.daffodil.processors.DIArray
import edu.illinois.ncsa.daffodil.processors.DINode

/**
 * Lets us pattern deconstruct infoset events, which is nice for unit testing
 */
object Start {
  def apply(node: DINode) = InfosetAccessor(StartKind, node)
  def unapply(ev: InfosetAccessor) = if (ev.kind eq StartKind) Some(ev.node) else None
}

object End {
  def apply(node: DINode) = InfosetAccessor(EndKind, node)
  def unapply(ev: InfosetAccessor) = if (ev.kind eq EndKind) Some(ev.node) else None
}

class TestInfosetCursor {

  /**
   * Compiles the schema to runtime data, then converts the infosetXML (our XML representation
   * of a Daffodil infoset) into an actual InfosetDocument object (Daffodil's native data structure),
   * and then creates an InfosetCursor which can be used to read out those nodes in-order.
   */
  def infosetCursor(testSchema: scala.xml.Node, infosetXML: scala.xml.Node) = {
    val compiler = Compiler()
    val pf = compiler.compileNode(testSchema)
    if (pf.isError) {
      val msgs = pf.getDiagnostics.map(_.getMessage()).mkString("\n")
      throw new Exception(msgs)
    }
    val u = pf.onPath("/").asInstanceOf[DataProcessor]
    if (u.isError) {
      val msgs = u.getDiagnostics.map(_.getMessage()).mkString("\n")
      throw new Exception(msgs)
    }
    val rootERD = u.ssrd.elementRuntimeData
    val infosetCursor = InfosetCursor.fromXMLNode(infosetXML, rootERD)
    infosetCursor
  }

  @Test def testInfosetCursorFromSimpleValue1() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>)
    val infosetXML = <foo xmlns={ XMLUtils.EXAMPLE_NAMESPACE }>Hello</foo>
    val ic = infosetCursor(sch, infosetXML)

    val aacc: InfosetAccessor = ic.advanceAccessor
    val iicc: InfosetAccessor = ic.inspectAccessor

    assertTrue(ic.advance)
    val Start(foo: DISimple) = aacc
    assertEquals("Hello", foo.dataValueAsString)

    assertTrue(ic.inspect)
    val End(foo_i: DISimple) = iicc
    assertTrue(foo_i eq foo)

    assertTrue(ic.advance)
    val End(foo_e: DISimple) = aacc
    assertTrue(foo_e eq foo)

    assertFalse(ic.inspect)
    assertFalse(ic.advance)
  }

  @Test def testInfosetCursorFromTreeNil() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element nillable="true" dfdl:nilValue="nil" dfdl:nilKind="literalValue" name="foo" dfdl:lengthKind="explicit" dfdl:length="3" type="xs:string"/>)
    val infosetXML = <foo xsi:nil="true" xmlns={ XMLUtils.EXAMPLE_NAMESPACE } xmlns:xsi={ XMLUtils.XSI_NAMESPACE }/>
    val ic = infosetCursor(sch, infosetXML)

    val aacc = ic.advanceAccessor
    val iicc: InfosetAccessor = ic.inspectAccessor

    assertTrue(ic.advance)
    val Start(foo: DISimple) = aacc
    assertTrue(foo.isNilled)

    assertTrue(ic.inspect)
    val End(foo_i: DISimple) = iicc
    assertTrue(foo_i eq foo)

    assertTrue(ic.advance)
    val End(foo_e: DISimple) = aacc
    assertTrue(foo_e eq foo)

    assertFalse(ic.inspect)
    assertFalse(ic.advance)
  }

  @Test def testInfosetCursorFromTreeComplex1() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><foo>Hello</foo></bar>

    val ic = infosetCursor(sch, infosetXML)

    val aacc = ic.advanceAccessor
    val iacc = ic.inspectAccessor

    assertTrue(ic.advance)
    val Start(bar: DIComplex) = aacc

    assertTrue(ic.advance)
    val Start(foo: DISimple) = aacc
    assertEquals("Hello", foo.dataValue)

    assertTrue(ic.inspect)
    val End(ifoo: DISimple) = iacc
    assertTrue(foo eq ifoo)

    assertTrue(ic.advance)
    val End(eFoo: DISimple) = aacc
    assertTrue(foo eq eFoo)

    assertTrue(ic.inspect)
    val End(ibar: DIComplex) = iacc
    assertTrue(bar eq ibar)

    assertTrue(ic.advance)
    val End(bar_e: DIComplex) = aacc
    assertTrue(bar eq bar_e)

    assertFalse(ic.inspect)
    assertFalse(ic.advance)
  }

  @Test def testInfosetComplex2() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
            <xs:element name="baz" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><foo>Hello</foo><baz>World</baz></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(bar_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(baz_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz_e: DISimple) = { assertTrue(ic.advance); aacc }; assertNotNull(baz_e)
    val End(bar_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s eq bar_e) // exact same object
    assertTrue(foo_s eq foo_e)
    assertTrue(foo_s.dataValue.isInstanceOf[String])
    assertTrue(foo_s.dataValueAsString =:= "Hello")
    assertTrue(baz_s.dataValue.isInstanceOf[String])
    assertTrue(baz_s.dataValueAsString =:= "World")
    assertEquals(bar_s.toXML(), infosetXML)
  }

  @Test def testInfosetComplex3() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="quux">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="bar1" dfdl:lengthKind="implicit">
              <xs:complexType>
                <xs:sequence>
                  <xs:element name="foo1" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                  <xs:element name="baz1" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                </xs:sequence>
              </xs:complexType>
            </xs:element>
            <xs:element name="bar2" dfdl:lengthKind="implicit">
              <xs:complexType>
                <xs:sequence>
                  <xs:element name="foo2" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                  <xs:element name="baz2" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                </xs:sequence>
              </xs:complexType>
            </xs:element>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <quux xmlns={ XMLUtils.EXAMPLE_NAMESPACE }>
                       <bar1><foo1>Hello</foo1><baz1>World</baz1></bar1>
                       <bar2><foo2>Hello</foo2><baz2>World</baz2></bar2>
                     </quux>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(quux_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(bar1_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo1_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(baz1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz1_e: DISimple) = { assertTrue(ic.advance); aacc }; assertNotNull(baz1_e)
    val End(bar1_e: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(bar2_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo2_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(baz2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz2_e: DISimple) = { assertTrue(ic.advance); aacc }; assertNotNull(baz2_e)
    val End(bar2_e: DIComplex) = { assertTrue(ic.advance); aacc }
    val End(quux_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar1_s eq bar1_e) // exact same object
    assertTrue(foo1_s eq foo1_e)
    assertTrue(foo1_s.dataValue.isInstanceOf[String])
    assertTrue(foo1_s.dataValueAsString =:= "Hello")
    assertTrue(baz1_s.dataValue.isInstanceOf[String])
    assertTrue(baz1_s.dataValueAsString =:= "World")
    assertTrue(bar2_s eq bar2_e) // exact same object
    assertTrue(foo2_s eq foo2_e)
    assertTrue(foo2_s.dataValue.isInstanceOf[String])
    assertTrue(foo2_s.dataValueAsString =:= "Hello")
    assertTrue(baz2_s.dataValue.isInstanceOf[String])
    assertTrue(baz2_s.dataValueAsString =:= "World")
    assertTrue(quux_s eq quux_e)
    TestUtils.assertEqualsXMLElements(quux_s.toXML()(0), infosetXML)
  }

  @Test def testInfosetArray1() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><foo>Hello</foo><foo>World</foo></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(bar_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo_arr_s: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(foo_1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_1_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(foo_2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_2_e: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_arr_e: DIArray) = { assertTrue(ic.advance); aacc }
    val End(bar_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s eq bar_e) // exact same object
    assertTrue(foo_arr_s eq foo_arr_e)
    assertTrue(foo_1_s eq foo_1_e)
    assertTrue(foo_1_s.dataValue.isInstanceOf[String])
    assertTrue(foo_1_s.dataValueAsString =:= "Hello")
    assertTrue(foo_2_s eq foo_2_e)
    assertTrue(foo_2_s.dataValue.isInstanceOf[String])
    assertTrue(foo_2_s.dataValueAsString =:= "World")
    assertEquals(bar_s.toXML(), infosetXML)
  }

  @Test def testInfosetArray2() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
            <xs:element name="baz" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><foo>Hello</foo><foo>World</foo><baz>Yadda</baz></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(bar_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo_arr_s: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(foo_1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_1_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(foo_2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_2_e: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_arr_e: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(baz_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz_e: DISimple) = { assertTrue(ic.advance); aacc }
    assertNotNull(baz_e)
    val End(bar_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s eq bar_e) // exact same object
    assertTrue(foo_arr_s eq foo_arr_e)
    assertTrue(foo_1_s eq foo_1_e)
    assertTrue(foo_1_s.dataValue.isInstanceOf[String])
    assertTrue(foo_1_s.dataValueAsString =:= "Hello")
    assertTrue(foo_2_s eq foo_2_e)
    assertTrue(foo_2_s.dataValue.isInstanceOf[String])
    assertTrue(foo_2_s.dataValueAsString =:= "World")
    assertTrue(baz_s.dataValue.isInstanceOf[String])
    assertTrue(baz_s.dataValueAsString =:= "Yadda")
    assertEquals(bar_s.toXML(), infosetXML)
  }

  @Test def testInfosetArray3() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="baz" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><baz>Yadda</baz><foo>Hello</foo><foo>World</foo></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(bar_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(baz_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz_e: DISimple) = { assertTrue(ic.advance); aacc }
    assertNotNull(baz_e)
    val Start(foo_arr_s: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(foo_1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_1_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(foo_2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_2_e: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_arr_e: DIArray) = { assertTrue(ic.advance); aacc }

    val End(bar_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s eq bar_e) // exact same object
    assertTrue(foo_arr_s eq foo_arr_e)
    assertTrue(foo_1_s eq foo_1_e)
    assertTrue(foo_1_s.dataValue.isInstanceOf[String])
    assertTrue(foo_1_s.dataValueAsString =:= "Hello")
    assertTrue(foo_2_s eq foo_2_e)
    assertTrue(foo_2_s.dataValue.isInstanceOf[String])
    assertTrue(foo_2_s.dataValueAsString =:= "World")
    assertTrue(baz_s.dataValue.isInstanceOf[String])
    assertTrue(baz_s.dataValueAsString =:= "Yadda")
    assertEquals(bar_s.toXML(), infosetXML)
  }

  @Test def testInfosetArray4() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="baz" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><baz>Yadda</baz><foo>Hello</foo><foo>World</foo></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(bar_s: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(baz_arr_s: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(baz_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(baz_e: DISimple) = { assertTrue(ic.advance); aacc }
    assertNotNull(baz_e)
    val End(baz_arr_e: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(foo_arr_s: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(foo_1_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_1_e: DISimple) = { assertTrue(ic.advance); aacc }
    val Start(foo_2_s: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_2_e: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_arr_e: DIArray) = { assertTrue(ic.advance); aacc }
    val End(bar_e: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s eq bar_e) // exact same object
    assertTrue(foo_arr_s eq foo_arr_e)
    assertTrue(baz_arr_s eq baz_arr_e)
    assertTrue(foo_1_s eq foo_1_e)
    assertTrue(foo_1_s.dataValue.isInstanceOf[String])
    assertTrue(foo_1_s.dataValueAsString =:= "Hello")
    assertTrue(foo_2_s eq foo_2_e)
    assertTrue(foo_2_s.dataValue.isInstanceOf[String])
    assertTrue(foo_2_s.dataValueAsString =:= "World")
    assertTrue(baz_s.dataValue.isInstanceOf[String])
    assertTrue(baz_s.dataValueAsString =:= "Yadda")
    assertEquals(bar_s.toXML(), infosetXML)
  }

  @Test def testInfosetComplexPeek1() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="bar" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="foo" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <bar xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><foo>Hello</foo></bar>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val iacc = ic.inspectAccessor
    val Start(bar_s1: DIComplex) = { assertTrue(ic.inspect); iacc }
    val Start(bar_s2: DIComplex) = { assertTrue(ic.inspect); iacc }
    val Start(bar_s3: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(foo_s1: DISimple) = { assertTrue(ic.inspect); iacc }
    val Start(foo_s2: DISimple) = { assertTrue(ic.advance); aacc }
    val End(foo_e: DISimple) = { assertTrue(ic.advance); aacc }
    val End(bar_e1: DIComplex) = { assertTrue(ic.inspect); iacc }
    assertTrue(ic.inspect)
    val End(bar_e2: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(bar_s1 eq bar_s2)
    assertTrue(bar_s2 eq bar_s3)
    assertTrue(bar_e1 eq bar_e2)
    assertTrue(bar_s1 eq bar_e1) // exact same object
    assertTrue(foo_s1 eq foo_s2)
    assertTrue(foo_s1 eq foo_e)
    assertTrue(foo_s1.dataValue.isInstanceOf[String])
    assertTrue(foo_s1.dataValueAsString =:= "Hello")
    assertEquals(bar_s1.toXML(), infosetXML)
  }

  @Test def testInfosetArrayComplex1() {
    val sch = SchemaUtils.dfdlTestSchema(
      <dfdl:format ref="tns:daffodilTest1"/>,
      <xs:element name="e" dfdl:lengthKind="implicit">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="s" minOccurs="0" maxOccurs="unbounded">
              <xs:complexType>
                <xs:choice>
                  <xs:element name="c1" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                  <xs:element name="c2" dfdl:lengthKind="explicit" dfdl:length="5" type="xs:string"/>
                </xs:choice>
              </xs:complexType>
            </xs:element>
          </xs:sequence>
        </xs:complexType>
      </xs:element>)
    val infosetXML = <e xmlns={ XMLUtils.EXAMPLE_NAMESPACE }><s><c1>Hello</c1></s><s><c2>World</c2></s></e>
    val ic = infosetCursor(sch, infosetXML)
    val aacc = ic.advanceAccessor
    val Start(e: DIComplex) = { assertTrue(ic.advance); aacc }
    val Start(as: DIArray) = { assertTrue(ic.advance); aacc }
    val Start(s1: DIComplex) = { assertTrue(ic.advance); aacc }; assertNotNull(s1)
    val Start(c1: DISimple) = { assertTrue(ic.advance); aacc }
    val End(c1e: DISimple) = { assertTrue(ic.advance); aacc }; assertNotNull(c1e)
    val End(s1e: DIComplex) = { assertTrue(ic.advance); aacc }; assertNotNull(s1e)
    val Start(s2: DIComplex) = { assertTrue(ic.advance); aacc }; assertNotNull(s2)
    val Start(c2: DISimple) = { assertTrue(ic.advance); aacc }
    val End(c2e: DISimple) = { assertTrue(ic.advance); aacc }; ; assertNotNull(c2e)
    val End(s2e: DIComplex) = { assertTrue(ic.advance); aacc }; assertNotNull(s2e)
    val End(ase: DIArray) = { assertTrue(ic.advance); aacc }
    val End(ee: DIComplex) = { assertTrue(ic.advance); aacc }
    assertFalse(ic.inspect)
    assertTrue(as eq ase) // exact same object
    assertTrue(e eq ee)
    assertTrue(c1.dataValue.isInstanceOf[String])
    assertTrue(c1.dataValueAsString =:= "Hello")
    assertTrue(c2.dataValue.isInstanceOf[String])
    assertTrue(c2.dataValueAsString =:= "World")
  }

}