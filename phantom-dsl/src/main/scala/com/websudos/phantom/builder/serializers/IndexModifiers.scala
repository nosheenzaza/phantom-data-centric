/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] class IndexModifiers extends BaseModifiers {

  def eqs(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def ==(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def lt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lt, value)
  }

  def lte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lte, value)
  }

  def gt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gt, value)
  }

  def gte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gte, value)
  }

  def in(column: String, values: String*): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def in(column: String, values: List[String]): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def fcall(name: String, params: String*): CQLQuery = {
    CQLQuery(name).append(Utils.join(params))
  }

  def token(name: String): String = {
    CQLQuery(CQLSyntax.token).wrap(name).queryString
  }

  def where(qb: CQLQuery, condition: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.where, condition)
  }

  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.and, clause)
  }

}