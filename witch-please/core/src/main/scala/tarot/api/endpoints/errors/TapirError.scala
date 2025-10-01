package tarot.api.endpoints.errors

import shared.api.dto.tarot.errors.TarotErrorResponse
import sttp.model.StatusCode
import sttp.tapir.EndpointOutput
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*

object TapirError {
  val tapirErrorOut: EndpointOutput.OneOf[TarotErrorResponse, TarotErrorResponse] = {
    val errorJson = jsonBody[TarotErrorResponse]
    oneOf[TarotErrorResponse](
      oneOfVariantValueMatcher(StatusCode.BadRequest, errorJson) {
        case TarotErrorResponse.BadRequestError(_) => true
      },
      oneOfVariantValueMatcher(StatusCode.NotFound, errorJson) {
        case TarotErrorResponse.NotFoundError(_) => true
      },
      oneOfVariantValueMatcher(StatusCode.Conflict, errorJson) {
        case TarotErrorResponse.ConflictError(_) => true
      },
      oneOfVariantValueMatcher(StatusCode.Unauthorized, errorJson) {
        case TarotErrorResponse.Unauthorized(_) => true
      },
      oneOfVariantValueMatcher(StatusCode.InternalServerError, errorJson) {
        case _: TarotErrorResponse.InternalServerError => true
      }
    )
  }
}
