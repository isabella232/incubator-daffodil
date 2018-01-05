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

package org.apache.daffodil.processors

import org.apache.daffodil.processors.unparsers.UState
import org.apache.daffodil.processors.unparsers.UStateMain
import org.apache.daffodil.exceptions.Assert
import org.apache.daffodil.util.MaybeULong
import org.apache.daffodil.io.DirectOrBufferedDataOutputStream
import org.apache.daffodil.util.Logging
import org.apache.daffodil.util.LogLevel
import org.apache.daffodil.util.Maybe
import org.apache.daffodil.util.Maybe._
import org.apache.daffodil.util.MaybeInt
import org.apache.daffodil.processors.unparsers.UnparseError
import org.apache.daffodil.io.BitOrderChangeException

/**
 * The suspension object keeps track of the state of the task, i.e., whether it
 * is done, whether it is making forward progress when run or not.
 *
 * A suspension" may block, by which we mean it may set isDone to false, and return.
 *
 * Running the suspension again tries again and will either block or complete.
 *
 */
trait Suspension
  extends Serializable with Logging {

  /**
   * Specifies that this suspension does not write to the data output stream.
   *
   * Override in TargetLengthOperation,and in SuspendableExpression as they
   * don't write to the DOS hence, if a DOS is created it can be setFinished
   * immediately.
   *
   * TODO: Redundant with implementing maybeKnownLengthInBits as MaybeULong(0L)
   */
  val isReadOnly = false

  def UE(ustate: UState, s: String, args: Any*) = {
    UnparseError(One(rd.schemaFileLocation), One(ustate.currentLocation), s, args: _*)
  }

  private var savedUstate_ : UState = null

  final def savedUstate = {
    Assert.invariant(savedUstate_ ne null)
    // Assert above fails if no suspension state created yet. Can't ask for ustate.
    // means we are pre-evaluating to decide if we need to suspend.
    savedUstate_
  }

  def rd: RuntimeData

  protected def maybeKnownLengthInBits(ustate: UState): MaybeULong = MaybeULong.Nope

  protected def doTask(ustate: UState): Unit

  /**
   * After calling this, call isDone and if that's false call isMakingProgress to
   * understand whether it is done, blocked on the exactly same situation, or blocked elsewhere.
   *
   * This status is needed to implement circular deadlock detection
   */
  final def runSuspension() {
    doTask(savedUstate)
    if (isDone && !isReadOnly) {
      try {
        //
        // We are done, and we're not readOnly, so the
        // DOS needs to be set finished now.
        //
        savedUstate.dataOutputStream.setFinished(savedUstate)
      } catch {
        case boc: BitOrderChangeException =>
          savedUstate.SDE(boc)
      }
      log(LogLevel.Debug, "%s finished %s.", this, savedUstate)
    }
  }

  /**
   * Run the first time.
   *
   */
  final def run(ustate: UState) {
    doTask(ustate)
    if (!isDone) {
      prepareToSuspend(ustate)
    }
  }

  private def prepareToSuspend(ustate: UState) {
    val mkl = maybeKnownLengthInBits(ustate)
    //
    // It seems like we have too many splits going on.
    //
    // As written, we have a bunch of suspensions that occur, but have
    // specifically known length of zero bits. So nothing being written out.
    // In that case, why do we need to split at all?
    //
    val original = ustate.dataOutputStream.asInstanceOf[DirectOrBufferedDataOutputStream]
    if (mkl.isEmpty || (mkl.isDefined && mkl.get > 0)) {
      //
      // only split if the length is either unknown
      // or known and greater than 0.
      //
      // If length known 0, then no need for another DOS
      //
      splitDOS(ustate, mkl, original)
    }
    suspend(ustate, original)
  }

  private def splitDOS(ustate: UState,
    maybeKnownLengthInBits: MaybeULong,
    original: DirectOrBufferedDataOutputStream) {
    Assert.usage(ustate.currentInfosetNodeMaybe.isDefined)

    val buffered = original.addBuffered

    if (maybeKnownLengthInBits.isDefined) {
      // since we know the length of the unparsed representation that we're skipping for now,
      // that means we know the absolute position of the bits in the buffer we're creating
      // and that means alignment operations don't have to suspend waiting for this knowledge
      if (original.maybeAbsBitPos0b.isDefined) {
        // direct streams always know this, but buffered streams may not.

        val originalAbsBitPos0b = original.maybeAbsBitPos0b.getULong

        // we are passed this length (in bits)
        // and can use it to initialize the absolute bit pos of the buffered output stream.
        //
        // This allows us to deal with alignment regions, that is, we can determine
        // their size since we know the absolute bit position.

        val mkl = maybeKnownLengthInBits.getULong
        buffered.setAbsStartingBitPos0b(originalAbsBitPos0b + mkl)
        buffered.setPriorBitOrder(ustate.bitOrder)

      }
    } else {
      log(LogLevel.Debug, "Buffered DOS created for %s without knowning absolute start bit pos: %s\n",
        ustate.currentInfosetNode.erd.diagnosticDebugName, buffered)
    }

    // the main-thread will carry on using the original ustate but unparsing
    // into this buffered stream.
    ustate.dataOutputStream = buffered
  }

  private def suspend(ustate: UState, original: DirectOrBufferedDataOutputStream) {
    //
    // clone the ustate for use when evaluating the expression
    //
    // TODO: Performance - copying this whole state, just for OVC is painful.
    // Some sort of copy-on-write scheme would be better.
    //
    val didSplit = (ustate.dataOutputStream ne original)
    val cloneUState = ustate.asInstanceOf[UStateMain].cloneForSuspension(original)
    if (isReadOnly && didSplit) {
      Assert.invariantFailed("Shouldn't have split. read-only case")
      //      try {
      //        // We did a DOS split, but we know we'll not be writing to it
      //        //
      //        // So we set finished immediately
      //        //
      //        // TODO: Begs the question of why we needed the split to begin with in that
      //        // case. Figure out why and document it!
      //        //
      //        original.setFinished(cloneUState)
      //      } catch {
      //        case boc: BitOrderChangeException => ustate.SDE(boc)
      //      }
    }

    savedUstate_ = cloneUState

    ustate.asInstanceOf[UStateMain].addSuspension(this)
  }

  final def explain() {
    val t = this
    Assert.invariant(t.isBlocked)
    log(LogLevel.Warning, "%s", t.blockedLocation)
  }

  private var priorNodeOrVar: Maybe[AnyRef] = Nope
  private var priorInfo: Maybe[AnyRef] = Nope
  private var priorIndex: MaybeInt = MaybeInt.Nope
  private var priorExc: Maybe[AnyRef] = Nope

  private var maybeNodeOrVar: Maybe[AnyRef] = Nope
  private var maybeInfo: Maybe[AnyRef] = Nope
  private var maybeIndex: MaybeInt = MaybeInt.Nope
  private var maybeExc: Maybe[AnyRef] = Nope

  private var done_ : Boolean = false
  private var isBlocked_ = false

  final def setDone {
    done_ = true
  }

  final def isDone = done_

  final def isBlocked = isBlocked_

  final def setUnblocked() {
    isBlocked_ = false
  }

  /**
   * False if the expression blocked at the same spot, i.e.,
   * didn't make any forward progress.
   */
  private var isMakingProgress_ : Boolean = true

  final def isMakingProgress = isMakingProgress_

  final def block(nodeOrVar: AnyRef, info: AnyRef, index: Long, exc: AnyRef) {
    log(LogLevel.Debug, "blocking %s due to %s", this, exc)

    Assert.usage(nodeOrVar ne null)
    Assert.usage(info ne null)
    Assert.usage(exc ne null)
    priorNodeOrVar = maybeNodeOrVar
    priorInfo = maybeInfo
    priorIndex = maybeIndex
    priorExc = maybeExc
    maybeNodeOrVar = One(nodeOrVar)
    maybeInfo = One(info)
    maybeIndex = MaybeInt(index.toInt)
    maybeExc = One(exc)
    done_ = false
    isBlocked_ = true

    if (isBlockedSameLocation) {
      isMakingProgress_ = false
    } else if (isBlockedFirstTime) {
      isMakingProgress_ = true
    } else {
      isMakingProgress_ = true
    }
  }

  final def blockedLocation = "BLOCKED\nexc=%s\nnode=%s\ninfo=%s\nindex=%s".format(maybeExc, maybeNodeOrVar, maybeInfo, maybeIndex)

  private def isBlockedFirstTime: Boolean = {
    isBlocked &&
      priorNodeOrVar.isEmpty
  }

  private def isBlockedSameLocation: Boolean = {
    val res = isBlocked &&
      {
        if (priorNodeOrVar.isEmpty) false
        else {
          Assert.invariant(maybeNodeOrVar.isDefined)
          val res =
            maybeNodeOrVar.get == priorNodeOrVar.get &&
              maybeInfo.get == priorInfo.get &&
              maybeIndex.get == priorIndex.get &&
              maybeExc.get == priorExc.get
          res
        }
      }
    res
  }

}
