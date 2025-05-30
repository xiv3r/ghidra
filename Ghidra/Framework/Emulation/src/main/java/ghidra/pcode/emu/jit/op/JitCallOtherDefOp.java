/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.pcode.emu.jit.op;

import java.util.List;

import ghidra.pcode.emu.jit.analysis.JitDataFlowState.MiniDFState;
import ghidra.pcode.emu.jit.analysis.JitTypeBehavior;
import ghidra.pcode.emu.jit.var.JitOutVar;
import ghidra.pcode.emu.jit.var.JitVal;
import ghidra.pcode.exec.PcodeUseropLibrary.PcodeUseropDefinition;
import ghidra.program.model.pcode.PcodeOp;

/**
 * The use-def node for a {@link PcodeOp#CALLOTHER} with an output operand.
 * 
 * @param op the p-code op
 * @param out the use-def variable node for the output
 * @param type the behavior of the output
 * @param userop the userop definition
 * @param args the list of use-def values nodes given as arguments
 * @param inputTypes the type behavior for each parameter
 * @param dfState the captured data flow state at the call site
 */
public record JitCallOtherDefOp(PcodeOp op, JitOutVar out, JitTypeBehavior type,
		PcodeUseropDefinition<Object> userop, List<JitVal> args, List<JitTypeBehavior> inputTypes,
		MiniDFState dfState) implements JitCallOtherOpIf, JitDefOp {

	@Override
	public boolean canBeRemoved() {
		return JitCallOtherOpIf.super.canBeRemoved() && JitDefOp.super.canBeRemoved();
	}

	@Override
	public void link() {
		JitDefOp.super.link();
		JitCallOtherOpIf.super.link();
	}

	@Override
	public void unlink() {
		JitDefOp.super.unlink();
		JitCallOtherOpIf.super.unlink();
	}
}
