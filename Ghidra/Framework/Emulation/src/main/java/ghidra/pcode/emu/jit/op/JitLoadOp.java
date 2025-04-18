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

import ghidra.pcode.emu.jit.analysis.JitTypeBehavior;
import ghidra.pcode.emu.jit.var.JitOutVar;
import ghidra.pcode.emu.jit.var.JitVal;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.pcode.PcodeOp;

/**
 * The use-def node for a {@link PcodeOp#LOAD}.
 * 
 * @param op the p-code op
 * @param out the use-def variable node for the output
 * @param space the address space
 * @param offset the use-def node for the offset
 */
public record JitLoadOp(PcodeOp op, JitOutVar out, AddressSpace space, JitVal offset)
		implements JitDefOp {

	@Override
	public void link() {
		JitDefOp.super.link();
		offset.addUse(this, 0);
	}

	@Override
	public void unlink() {
		JitDefOp.super.unlink();
		offset.removeUse(this, 0);
	}

	@Override
	public List<JitVal> inputs() {
		return List.of(offset);
	}

	@Override
	public JitTypeBehavior typeFor(int position) {
		return switch (position) {
			case 0 -> offsetType();
			default -> throw new AssertionError();
		};
	}

	/**
	 * We'd like the offset to be an {@link JitTypeBehavior#INTEGER int}.
	 * 
	 * @return {@link JitTypeBehavior#INTEGER}
	 */
	public JitTypeBehavior offsetType() {
		return JitTypeBehavior.INTEGER;
	}

	@Override
	public JitTypeBehavior type() {
		return JitTypeBehavior.ANY;
	}
}
