package edu.illinois.ncsa.daffodil.processors

import edu.illinois.ncsa.daffodil.dsom._
import edu.illinois.ncsa.daffodil.grammar.EmptyGram
import edu.illinois.ncsa.daffodil.grammar._

object PrimitiveFactory extends PrimitiveFactoryBase {

  def prod(nameArg: String, sc: Term, guard: Boolean, gram: => Gram): Gram =
    if (guard) gram else EmptyGram

  def ChoiceElementBegin(e: ElementBase): Terminal = new ChoiceElementBegin(e)
  def ElementBegin(e: ElementBase): Terminal = new ElementBegin(e)
  //def ComplexElementBeginPattern(e: ElementBase) : Terminal = ComplexElementBeginPattern(e)
  def ChoiceElementEnd(e: ElementBase): Terminal = new ChoiceElementEnd(e)
  def ElementEnd(e: ElementBase): Terminal = new ElementEnd(e)

  def ElementEndNoRep(e: ElementBase): Terminal = new ElementEndNoRep(e)
  //def ComplexElementEndPattern(e: ElementBase): Terminal = new ComplexElementEndPattern(e)
  def ConvertTextIntegerPrim(e: ElementBase): Terminal = new ConvertTextIntegerPrim(e)
  def ConvertTextDecimalPrim(e: ElementBase): Terminal = new ConvertTextDecimalPrim(e)
  def ConvertTextNonNegativeIntegerPrim(e: ElementBase): Terminal = new ConvertTextNonNegativeIntegerPrim(e)
  def ConvertTextLongPrim(e: ElementBase): Terminal = new ConvertTextLongPrim(e)
  def ConvertTextIntPrim(e: ElementBase): Terminal = new ConvertTextIntPrim(e)
  def ConvertTextShortPrim(e: ElementBase): Terminal = new ConvertTextShortPrim(e)
  def ConvertTextBytePrim(e: ElementBase): Terminal = new ConvertTextBytePrim(e)
  def ConvertTextUnsignedLongPrim(e: ElementBase): Terminal = new ConvertTextUnsignedLongPrim(e)
  def ConvertTextUnsignedIntPrim(e: ElementBase): Terminal = new ConvertTextUnsignedIntPrim(e)
  def ConvertTextUnsignedShortPrim(e: ElementBase): Terminal = new ConvertTextUnsignedShortPrim(e)
  def ConvertTextUnsignedBytePrim(e: ElementBase): Terminal = new ConvertTextUnsignedBytePrim(e)
  def ConvertTextDoublePrim(e: ElementBase): Terminal = new ConvertTextDoublePrim(e)
  def ConvertTextFloatPrim(e: ElementBase): Terminal = new ConvertTextFloatPrim(e)
  def ZonedTextBytePrim(el: ElementBase): Terminal = new ZonedTextBytePrim(el)
  def ZonedTextShortPrim(el: ElementBase): Terminal = new ZonedTextShortPrim(el)
  def ZonedTextIntPrim(el: ElementBase): Terminal = new ZonedTextIntPrim(el)
  def ZonedTextLongPrim(el: ElementBase): Terminal = new ZonedTextLongPrim(el)
  def UnsignedRuntimeLengthRuntimeByteOrderBinaryNumber[T](e: ElementBase): Terminal = new UnsignedRuntimeLengthRuntimeByteOrderBinaryNumber[T](e)
  def UnsignedKnownLengthRuntimeByteOrderBinaryNumber[T](e: ElementBase, len: Long): Terminal = new UnsignedKnownLengthRuntimeByteOrderBinaryNumber[T](e, len)
  def SignedRuntimeLengthRuntimeByteOrderBinaryNumber[T](e: ElementBase): Terminal = new SignedRuntimeLengthRuntimeByteOrderBinaryNumber[T](e)
  def SignedKnownLengthRuntimeByteOrderBinaryNumber[T](e: ElementBase, len: Long): Terminal = new SignedKnownLengthRuntimeByteOrderBinaryNumber[T](e, len)
  def HexBinaryKnownLengthBinaryNumber(e: ElementBase, len: Long): Terminal = new HexBinaryKnownLengthBinaryNumber(e, len)
  def HexBinaryRuntimeLengthBinaryNumber(e: ElementBase): Terminal = new HexBinaryRuntimeLengthBinaryNumber(e)
  def FloatKnownLengthRuntimeByteOrderBinaryNumber(e: ElementBase, len: Long): Terminal = new FloatKnownLengthRuntimeByteOrderBinaryNumber(e, len)
  def DoubleKnownLengthRuntimeByteOrderBinaryNumber(e: ElementBase, len: Long): Terminal = new DoubleKnownLengthRuntimeByteOrderBinaryNumber(e, len)
  def DecimalKnownLengthRuntimeByteOrderBinaryNumber(e: ElementBase, len: Long): Terminal = new DecimalKnownLengthRuntimeByteOrderBinaryNumber(e, len)
  def PackedIntPrim(e: ElementBase): Terminal = new PackedIntPrim(e)
  def BCDIntPrim(e: ElementBase): Terminal = new BCDIntPrim(e)
  def StartChildren(ct: ComplexTypeBase, guard: Boolean = true): Terminal = new StartChildren(ct: ComplexTypeBase, guard)
  def StartSequence(sq: edu.illinois.ncsa.daffodil.dsom.Sequence, guard: Boolean = true): Terminal = new StartSequence(sq: edu.illinois.ncsa.daffodil.dsom.Sequence, guard)
  def Nada(sc: Term): Terminal = new Nada(sc)
  def OptionalInfixSep(term: Term, sep: => Gram, guard: Boolean = true): Terminal = new OptionalInfixSep(term, sep, guard)
  def EndChildren(ct: ComplexTypeBase, guard: Boolean = true): Terminal = new EndChildren(ct: ComplexTypeBase, guard)
  def EndSequence(sq: edu.illinois.ncsa.daffodil.dsom.Sequence, guard: Boolean = true): Terminal = new EndSequence(sq: edu.illinois.ncsa.daffodil.dsom.Sequence, guard)
  def StartArray(e: ElementBase, guard: Boolean = true): Terminal = new StartArray(e, guard)
  def EndArray(e: ElementBase, guard: Boolean = true): Terminal = new EndArray(e, guard)
  def NoValue(e: GlobalElementDecl, guard: Boolean = true): Terminal = new NoValue(e: GlobalElementDecl, guard)
  def SaveInputStream(e: ElementBase, guard: Boolean = true): Terminal = new SaveInputStream(e, guard)
  def SetEmptyInputStream(e: ElementBase, guard: Boolean = true): Terminal = new SetEmptyInputStream(e, guard)
  def RestoreInputStream(e: ElementBase, guard: Boolean = true): Terminal = new RestoreInputStream(e, guard)
  //def Value(e: SchemaComponent, guard: Boolean = true): Terminal = new Value(e: SchemaComponent, guard)
  def NotStopValue(e: ElementBase with LocalElementMixin): Terminal = new NotStopValue(e)
  def StopValue(e: ElementBase with LocalElementMixin): Terminal = new StopValue(e)
  def TheDefaultValue(e: ElementBase): Terminal = new TheDefaultValue(e)
  def LeadingSkipRegion(e: Term): Terminal = new LeadingSkipRegion(e)
  def AlignmentFill(e: Term): Terminal = new AlignmentFill(e)
  def TrailingSkipRegion(e: Term): Terminal = new TrailingSkipRegion(e)
  def PrefixLength(e: ElementBase): Terminal = new PrefixLength(e)
  def UnicodeByteOrderMark(e: GlobalElementDecl): Terminal = new UnicodeByteOrderMark(e)
  def FinalUnusedRegion(e: ElementBase): Terminal = new FinalUnusedRegion(e)
  def NewVariableInstanceStart(decl: AnnotatedSchemaComponent, stmt: DFDLNewVariableInstance): Terminal = new NewVariableInstanceStart(decl: AnnotatedSchemaComponent, stmt)
  def NewVariableInstanceEnd(decl: AnnotatedSchemaComponent, stmt: DFDLNewVariableInstance): Terminal = new NewVariableInstanceEnd(decl: AnnotatedSchemaComponent, stmt)
  def AssertPatternPrim(decl: AnnotatedSchemaComponent, stmt: DFDLAssert): Terminal = new AssertPatternPrim(decl: AnnotatedSchemaComponent, stmt)
  def DiscriminatorPatternPrim(decl: AnnotatedSchemaComponent, stmt: DFDLAssertionBase): Terminal = new DiscriminatorPatternPrim(decl: AnnotatedSchemaComponent, stmt)
  def AssertBooleanPrim(decl: AnnotatedSchemaComponent, stmt: DFDLAssertionBase): Terminal = new AssertBooleanPrim(decl: AnnotatedSchemaComponent, stmt)
  def DiscriminatorBooleanPrim(decl: AnnotatedSchemaComponent, stmt: DFDLAssertionBase): Terminal = new DiscriminatorBooleanPrim(decl: AnnotatedSchemaComponent, stmt)
  def InitiatedContent(decl: AnnotatedSchemaComponent): Terminal = new InitiatedContent(decl)
  def SetVariable(decl: AnnotatedSchemaComponent, stmt: DFDLSetVariable): Terminal = new SetVariable(decl: AnnotatedSchemaComponent, stmt)
  def InputValueCalc(e: ElementBase): Terminal = new InputValueCalc(e)

  def StaticInitiator(e: Term): Terminal = new StaticInitiator(e)
  def StaticTerminator(e: Term): Terminal = new StaticTerminator(e)
  def DynamicInitiator(e: Term): Terminal = new DynamicInitiator(e)
  def DynamicTerminator(e: Term): Terminal = new DynamicTerminator(e)
  def StaticSeparator(s: edu.illinois.ncsa.daffodil.dsom.Sequence, t: Term): Terminal = new StaticSeparator(s: edu.illinois.ncsa.daffodil.dsom.Sequence, t)
  def DynamicSeparator(s: edu.illinois.ncsa.daffodil.dsom.Sequence, t: Term): Terminal = new DynamicSeparator(s: edu.illinois.ncsa.daffodil.dsom.Sequence, t)
  def LiteralNilExplicitLengthInBytes(e: ElementBase): Terminal = new LiteralNilExplicitLengthInBytes(e)
  def LiteralNilKnownLengthInBytes(e: ElementBase, lengthInBytes: Long): Terminal = new LiteralNilKnownLengthInBytes(e, lengthInBytes)
  def LiteralNilExplicitLengthInChars(e: ElementBase): Terminal = new LiteralNilExplicitLengthInChars(e)
  def LiteralNilExplicit(e: ElementBase, nUnits: Long): Terminal = new LiteralNilExplicit(e, nUnits)
  def LiteralNilPattern(e: ElementBase): Terminal = new LiteralNilPattern(e)
  def LiteralNilDelimitedEndOfDataStatic(eb: ElementBase): Terminal = new LiteralNilDelimitedEndOfDataStatic(eb)
  def LiteralNilDelimitedEndOfDataDynamic(eb: ElementBase): Terminal = new LiteralNilDelimitedEndOfDataDynamic(eb)
  def LogicalNilValue(e: ElementBase): Terminal = new LogicalNilValue(e)

  def ConvertTextDatePrim(e: ElementBase): Terminal = new ConvertTextDatePrim(e)
  def ConvertTextTimePrim(e: ElementBase): Terminal = new ConvertTextTimePrim(e)
  def ConvertTextDateTimePrim(e: ElementBase): Terminal = new ConvertTextDateTimePrim(e)

  def HexBinaryFixedLengthInBytes(e: ElementBase, nBytes: Long): Terminal = new HexBinaryFixedLengthInBytes(e, nBytes)
  def HexBinaryFixedLengthInBits(e: ElementBase, nBits: Long): Terminal = new HexBinaryFixedLengthInBits(e, nBits)
  def HexBinaryVariableLengthInBytes(e: ElementBase): Terminal = new HexBinaryVariableLengthInBytes(e)
  def StringFixedLengthInBytesFixedWidthCharacters(e: ElementBase, nBytes: Long): Terminal = new StringFixedLengthInBytesFixedWidthCharacters(e, nBytes)
  def StringFixedLengthInBytesVariableWidthCharacters(e: ElementBase, nBytes: Long): Terminal = new StringFixedLengthInBytesVariableWidthCharacters(e, nBytes)
  def StringFixedLengthInVariableWidthCharacters(e: ElementBase, numChars: Long): Terminal = new StringFixedLengthInVariableWidthCharacters(e, numChars)
  def StringVariableLengthInBytes(e: ElementBase): Terminal = new StringVariableLengthInBytes(e)
  def StringVariableLengthInBytesVariableWidthCharacters(e: ElementBase): Terminal = new StringVariableLengthInBytesVariableWidthCharacters(e)
  def StringVariableLengthInVariableWidthCharacters(e: ElementBase): Terminal = new StringVariableLengthInVariableWidthCharacters(e)
  def StringPatternMatched(e: ElementBase): Terminal = new StringPatternMatched(e)
  def StringDelimitedEndOfDataStatic(e: ElementBase): Terminal = new StringDelimitedEndOfDataStatic(e)
  def StringDelimitedEndOfDataDynamic(e: ElementBase): Terminal = new StringDelimitedEndOfDataDynamic(e)
  def HexBinaryDelimitedEndOfDataStatic(e: ElementBase): Terminal = new HexBinaryDelimitedEndOfDataStatic(e)
  def HexBinaryDelimitedEndOfDataDynamic(e: ElementBase): Terminal = new HexBinaryDelimitedEndOfDataDynamic(e)

  def SpecifiedLengthPattern(e: ElementBase, eGram: => Gram): Terminal = new SpecifiedLengthPattern(e, eGram)
  def SpecifiedLengthExplicitBitsFixed(e: ElementBase, eGram: => Gram, nBits: Long): Terminal = new SpecifiedLengthExplicitBitsFixed(e, eGram, nBits)
  def SpecifiedLengthExplicitBits(e: ElementBase, eGram: => Gram): Terminal = new SpecifiedLengthExplicitBits(e, eGram)
  def SpecifiedLengthExplicitBytesFixed(e: ElementBase, eGram: => Gram, nBytes: Long): Terminal = new SpecifiedLengthExplicitBytesFixed(e, eGram, nBytes)
  def SpecifiedLengthExplicitBytes(e: ElementBase, eGram: => Gram): Terminal = new SpecifiedLengthExplicitBytes(e, eGram)
  def SpecifiedLengthExplicitCharactersFixed(e: ElementBase, eGram: => Gram, nChars: Long): Terminal = new SpecifiedLengthExplicitCharactersFixed(e, eGram, nChars)
  def SpecifiedLengthExplicitCharacters(e: ElementBase, eGram: => Gram): Terminal = new SpecifiedLengthExplicitCharacters(e, eGram)

  def StmtEval(context: ElementBase, eGram: Gram): Gram = edu.illinois.ncsa.daffodil.processors.StmtEval(context, eGram) // must call object. Does an optimization.

  def UnorderedSequence(context: Term, eGram: Gram): Gram = edu.illinois.ncsa.daffodil.processors.UnorderedSequence(context, eGram)

  def RepExactlyN(context: LocalElementBase, n: Long, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepExactlyN(context, n, r)
  def RepAtMostTotalN(context: LocalElementBase, n: Long, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepAtMostTotalN(context, n, r)
  def RepExactlyTotalN(context: LocalElementBase, n: Long, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepExactlyTotalN(context, n, r)
  def RepUnbounded(context: LocalElementBase, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepUnbounded(context, r)
  def OccursCountExpression(e: ElementBase): Terminal = new OccursCountExpression(e)
  def RepAtMostOccursCount(e: LocalElementBase, n: Long, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepAtMostOccursCount(e, n, r)
  def RepExactlyTotalOccursCount(e: LocalElementBase, r: => Gram): Gram = edu.illinois.ncsa.daffodil.processors.RepExactlyTotalOccursCount(e, r)

}
