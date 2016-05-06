package edu.illinois.ncsa.daffodil.io

import junit.framework.Assert._
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.BitOrder
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.ByteOrder
import org.junit.Test

class TestDataOutputStream {

  @Test def testPutLongDirect1_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(-1L, 32)

    out.flush()

    val buf = baos.getBuf()

    assertEquals(-1, buf(0))
    assertEquals(-1, buf(1))
    assertEquals(-1, buf(2))
    assertEquals(-1, buf(3))
    if (buf.length > 4)
      assertEquals(0, buf(4))

  }

  @Test def testPutLongDirect1Bit_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(1, 1)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x80.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirect2Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(3, 2)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xC0.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirect7Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(0xA5, 7)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x4A.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirect8Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(0xA5, 8)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xA5.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirect9Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(0xA5A5, 9)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xD2.toByte, buf(0))
    assertEquals(0x80.toByte, buf(1))
    if (buf.length > 2)
      assertEquals(0, buf(2))

  }

  @Test def testPutLongDirect63Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(0xA5A5A5A5A5A5A5A5L, 63)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    val res = 0x25A5A5A5A5A5A5A5L << 1
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    if (buf.length > 8)
      assertEquals(0, buf(9))

  }

  @Test def testPutLongDirect64Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(0xA5A5A5A5A5A5A5A5L, 64)

    out.flush()
    out.setFinished()

    val buf = baos.getBuf()

    val res = 0xA5A5A5A5A5A5A5A5L
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    if (buf.length > 8)
      assertEquals(0, buf(9))

  }

  /////////////////////////////////////////////////////
  // Tests of Buffered and putLong
  /////////////////////////////////////////////////////

  @Test def testPutLongBuffered1_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(-1L, 32)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(-1, buf(0))
    assertEquals(-1, buf(1))
    assertEquals(-1, buf(2))
    assertEquals(-1, buf(3))
    if (buf.length > 4)
      assertEquals(0, buf(4))

  }

  @Test def testPutLongBuffered1Bit_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(1, 1)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x80.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongBuffered2Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(3, 2)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xC0.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongBuffered7Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5, 7)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x4A.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongBuffered8Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5, 8)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xA5.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongBuffered9Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5, 9)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xD2.toByte, buf(0))
    assertEquals(0x80.toByte, buf(1))
    if (buf.length > 2)
      assertEquals(0, buf(2))

  }

  @Test def testPutLongBuffered63Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5A5A5A5A5A5A5L, 63)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    val res = 0x25A5A5A5A5A5A5A5L << 1
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    if (buf.length > 8)
      assertEquals(0, buf(9))

  }

  @Test def testPutLongBuffered64Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5A5A5A5A5A5A5L, 64)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    val res = 0xA5A5A5A5A5A5A5A5L
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    if (buf.length > 8)
      assertEquals(0, buf(8))

  }

  /////////////////////////////////////////////////////
  // Tests of Direct + Buffered and putLong
  /////////////////////////////////////////////////////

  @Test def testPutLongDirectAndBuffered1_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(-1L, 32)
    direct.putLong(-1L, 32)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(-1, buf(0))
    assertEquals(-1, buf(1))
    assertEquals(-1, buf(2))
    assertEquals(-1, buf(3))
    assertEquals(-1, buf(4))
    assertEquals(-1, buf(5))
    assertEquals(-1, buf(6))
    assertEquals(-1, buf(7))
    if (buf.length > 8)
      assertEquals(0, buf(8))

  }

  @Test def testPutLongDirectAndBuffered1Bit_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(1, 1)
    direct.putLong(1, 1)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xC0.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirectAndBuffered2Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(3, 2)
    direct.putLong(2, 2)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xB0.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

  @Test def testPutLongDirectAndBuffered7Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5, 7)
    direct.putLong(0xA5, 7)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x4A.toByte, buf(0))
    assertEquals(0x94.toByte, buf(1))
    if (buf.length > 2)
      assertEquals(0, buf(2))

  }

  @Test def testPutLongDirectAndBuffered8Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5, 8)
    direct.putLong(0xA5, 8)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0xA5.toByte, buf(0))
    assertEquals(0xA5.toByte, buf(1))
    if (buf.length > 2)
      assertEquals(0, buf(2))

  }

  @Test def testPutLongDirectAndBuffered9Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xDEAD, 9)
    direct.putLong(0xBEEF, 9)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x77.toByte, buf(0))
    assertEquals(0xAB.toByte, buf(1))
    assertEquals(0x40.toByte, buf(2))
    if (buf.length > 3)
      assertEquals(0, buf(3))

  }

  @Test def testPutLongDirectAndBuffered63BitPlus1Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5A5A5A5A5A5A5L, 63)
    direct.putLong(1, 1)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    val res = 0xA5A5A5A5A5A5A5A5L
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    if (buf.length > 8)
      assertEquals(0, buf(9))

  }

  @Test def testPutLongDirectAndBuffered63BitPlus63Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5A5A5A5A5A5A5L, 63)
    direct.putLong(0xA5A5A5A5A5A5A5A5L, 63)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    val res1 = 0x4B4B4B4B4B4B4B4AL
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res1 >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    val res2 = 0x9696969696969694L
    i = 0
    while (i < 8) {
      val expected = "%x".format((res2 >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i + 8))
      assertEquals(expected, actual)
      i += 1
    }

    if (buf.length > 16)
      assertEquals(0, buf(16))

  }

  @Test def testPutLongDirectAndBuffered64BitPlus64Bit_BE_MSBF {
    val baos = new ByteArrayOutputStreamWithGetBuf()
    val direct = DirectOrBufferedDataOutputStream(baos)
    direct.setBitOrder(BitOrder.MostSignificantBitFirst)
    direct.setByteOrder(ByteOrder.BigEndian)

    val out = direct.addBuffered

    out.putLong(0xA5A5A5A5A5A5A5A5L, 64)
    direct.putLong(0xA5A5A5A5A5A5A5A5L, 64)

    direct.setFinished()
    out.setFinished()

    val buf = baos.getBuf()

    val res1 = 0xA5A5A5A5A5A5A5A5L
    var i = 0
    while (i < 8) {
      val expected = "%x".format((res1 >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i))
      assertEquals(expected, actual)
      i += 1
    }
    val res2 = 0xA5A5A5A5A5A5A5A5L
    i = 0
    while (i < 8) {
      val expected = "%x".format((res2 >> (56 - (8 * i)) & 0xFF).toByte)
      val actual = "%x".format(buf(i + 8))
      assertEquals(expected, actual)
      i += 1
    }

    if (buf.length > 16)
      assertEquals(0, buf(16))

  }

  @Test def testPutLong5_4Bits_BE_MSBF {

    val baos = new ByteArrayOutputStreamWithGetBuf()
    val out = DirectOrBufferedDataOutputStream(baos)

    out.setBitOrder(BitOrder.MostSignificantBitFirst)
    out.setByteOrder(ByteOrder.BigEndian)

    out.putLong(5L, 4)

    out.setFinished()

    val buf = baos.getBuf()

    assertEquals(0x50.toByte, buf(0))
    if (buf.length > 1)
      assertEquals(0, buf(1))

  }

}
