import java.nio.charset.{Charset, StandardCharsets}

import play.api.libs.ws.WSResponse

/**
  * Created by borisbondarenko on 18.06.16.
  */
package object crawling {

  implicit class WSResponseImprovement(val response: WSResponse) {
    def bodyAsUTF8: String = new String(response.bodyAsBytes.toArray, StandardCharsets.UTF_8)
  }
}
