package markov

import org.neo4j.graphdb.{GraphDatabaseService, Direction, Node, Relationship, PropertyContainer, DynamicRelationshipType}
import org.neo4j.kernel.Traversal

import javax.ws.rs.{Path, POST, Produces, FormParam, Consumes}
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType

import scala.collection.JavaConverters._

import net.liftweb.json._
import net.liftweb.json.Extraction._

@Path("/")
class markov(@Context db:GraphDatabaseService) {

  implicit val formats = net.liftweb.json.DefaultFormats

  val probType = DynamicRelationshipType.withName( "PROB" )

  @POST
  @Consumes(Array("text/plain", "application/json"))
  @Path("/markov/")
  @Produces(Array("application/json"))
  def findCaused(json:String) = {
    val tx = db.beginTx
    try {
      val params:Map[String,Any] = Serialization.read[Map[String,Any]](json)

      val paths = params
      Response.ok(compact(render(decompose(paths))), MediaType.APPLICATION_JSON).build()
    } catch {
      case e:Exception => Response.status(500).entity(e.getMessage).build()
    } finally {
      tx.finish
    }
  }

}
