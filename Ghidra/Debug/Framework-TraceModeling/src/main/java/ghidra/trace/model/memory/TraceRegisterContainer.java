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
package ghidra.trace.model.memory;

import ghidra.trace.model.target.iface.TraceObjectInterface;
import ghidra.trace.model.target.info.TraceObjectInfo;
import ghidra.trace.model.target.schema.TraceObjectSchema;

/**
 * A container of registers.
 * 
 * <p>
 * NOTE: This is a special case of "container", since it need not be the immediate parent of the
 * {@link TraceRegister}s it contains. Thus, this cannot be supplanted by
 * {@link TraceObjectSchema#searchForCanonicalContainer(Class)}.
 * 
 * @see TraceRegister
 */
@TraceObjectInfo(
	schemaName = "RegisterContainer",
	shortName = "register container",
	attributes = {},
	fixedKeys = {})
public interface TraceRegisterContainer extends TraceObjectInterface {
}
