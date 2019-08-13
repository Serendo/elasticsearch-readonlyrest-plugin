/*
 *    This file is part of ReadonlyREST.
 *
 *    ReadonlyREST is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    ReadonlyREST is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with ReadonlyREST.  If not, see http://www.gnu.org/licenses/
 */
package tech.beshu.ror.acl.factory.decoders.rules

import tech.beshu.ror.acl.blocks.rules.XForwardedForRule
import tech.beshu.ror.acl.blocks.variables.runtime.RuntimeMultiResolvableVariable
import tech.beshu.ror.acl.domain.Address
import tech.beshu.ror.acl.factory.decoders.common._
import tech.beshu.ror.acl.factory.decoders.rules.RuleBaseDecoder.RuleDecoderWithoutAssociatedFields
import tech.beshu.ror.acl.orders._
import tech.beshu.ror.acl.utils.CirceOps.DecoderHelpers

class XForwardedForRuleDecoder extends RuleDecoderWithoutAssociatedFields(
  DecoderHelpers
    .decodeStringLikeOrNonEmptySet[RuntimeMultiResolvableVariable[Address]]
    .map(addresses => new XForwardedForRule(XForwardedForRule.Settings(addresses)))
)