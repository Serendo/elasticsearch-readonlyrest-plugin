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
package tech.beshu.ror.utils.containers

import java.util.Optional

import cats.data.NonEmptyList
import org.apache.http.message.BasicHeader
import tech.beshu.ror.utils.containers.EsContainer.Credentials
import tech.beshu.ror.utils.containers.EsContainer.Credentials.{BasicAuth, Token}
import tech.beshu.ror.utils.containers.providers.ClientProvider.adminCredentials
import tech.beshu.ror.utils.httpclient.RestClient
import tech.beshu.ror.utils.misc.Tuple

object providers {

  trait ClientProvider {
    def basicAuthClient(user: String, pass: String): RestClient = client(Credentials.BasicAuth(user, pass))

    def tokenAuthClient(token: String): RestClient = client(Credentials.Token(token))

    def noBasicAuthClient: RestClient = client(Credentials.None)

    def adminClient: RestClient = basicAuthClient(adminCredentials._1, adminCredentials._2)

    private[providers] def client(credentials: Credentials): RestClient
  }

  object ClientProvider {
    val adminCredentials: (String, String) = ("admin", "container")
  }

  trait RorConfigFileNameProvider {
    implicit def rorConfigFileName: String
  }

  trait NodeInitializerProvider {
    def nodeDataInitializer: Option[ElasticsearchNodeDataInitializer] = None
  }

  trait MultipleClients {
    def clients: NonEmptyList[ClientProvider]
  }

  trait MultipleEsTargets {
    def esTargets: NonEmptyList[EsContainer]
  }

  trait SingleEsTarget extends MultipleEsTargets {
    def targetEs: EsContainer

    override def esTargets: NonEmptyList[EsContainer] = NonEmptyList.one(targetEs)
  }

  trait SingleClient extends ClientProvider with MultipleClients {

    override private[providers] def client(credentials: Credentials): RestClient =
      clients.head.client(credentials)
  }

  trait CallingEsDirectly extends MultipleClients {
    this: MultipleEsTargets =>

    override def clients: NonEmptyList[ClientProvider] = {
      esTargets.map { target =>
        (credentials: Credentials) => target.client(credentials)
      }
    }
  }

  trait CallingProxy extends MultipleClients {
    def proxyPorts: NonEmptyList[Int]

    override def clients: NonEmptyList[ClientProvider] = {
      proxyPorts.map(createProxyClient)
    }

    private def createProxyClient(port: Int): ClientProvider = {
      case BasicAuth(user, password) => new RestClient(false, "localhost", port, Optional.of(Tuple.from(user, password)))
      case Token(token) => new RestClient(false, "localhost", port, Optional.empty[Tuple[String, String]](), new BasicHeader("Authorization", token))
      case Credentials.None => new RestClient(false, "localhost", port, Optional.empty[Tuple[String, String]]())
    }
  }
}