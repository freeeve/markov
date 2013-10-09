package markov

import org.neo4j.graphdb.{GraphDatabaseService, Direction, Node, Relationship, PropertyContainer, DynamicRelationshipType}
import org.neo4j.kernel.Traversal
import org.neo4j.cypher.ExecutionEngine

import javax.ws.rs.{Path, POST, Produces, FormParam, Consumes}
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType

import scala.collection.JavaConverters._
import scala.annotation.tailrec

import net.liftweb.json._
import net.liftweb.json.Extraction._

case class ChainParams(lookup:String, length:Int)
case class MarkovChain(path:List[Long], prob:Double)
case class MarkovResult(start:Long, end:Long, prob:Double)

@Path("/")
class markov(@Context db:GraphDatabaseService) {

  implicit val formats = Serialization.formats(NoTypeHints)

  val probType = DynamicRelationshipType.withName( "PROB" )

  @POST
  @Consumes(Array("text/plain", "application/json"))
  @Path("/chains")
  @Produces(Array("application/json"))
  def chains(json:String) = {
    val tx = db.beginTx
    try {
      val params = parse(json).extract[ChainParams]
      val ee = new ExecutionEngine(db)
      val startNodes = ee.execute(params.lookup)
      val length = params.length

      var paths = List[MarkovChain]()
      for(start <- startNodes) {
        val startId = start.head._2.asInstanceOf[Node].getId
        traverse(startId, 0, MarkovChain(List[Long](startId), 1.0))
      }

      def traverse(curId:Long, len:Int, chain:MarkovChain):Unit = {
        //println("curId: " + curId + "; len: " + len + "; chain: " + chain)
        if(len == length) paths = paths :+ chain
        else {
          val n = db.getNodeById(curId)
          for(x <- n.getRelationships(Direction.OUTGOING, probType).asScala) {
            val otherId = x.getOtherNode(n).getId
            traverse(otherId, len + 1, chain.copy(chain.path :+ otherId, chain.prob * x.getProperty("p").asInstanceOf[Double]))
          }
        }
      }
 
      tx.success
      Response.ok(compact(render(decompose(paths))), MediaType.APPLICATION_JSON).build()
    } catch {
      case e:Exception => Response.status(500).entity(e.getMessage).build()
    } finally {
      tx.close
    }
  }

  @POST
  @Consumes(Array("text/plain", "application/json"))
  @Path("/results")
  @Produces(Array("application/json"))
  def results(json:String) = {
    val tx = db.beginTx
    try {
      val params = parse(json).extract[ChainParams]
      val ee = new ExecutionEngine(db)
      val startNodes = ee.execute(params.lookup)
      val length = params.length

      var paths = collection.mutable.Map[(Long,Long), Double]()
      for(start <- startNodes) {
        val startId = start.head._2.asInstanceOf[Node].getId
        traverse(startId, startId, 0, 1.0)
      }

      def traverse(startId:Long, curId:Long, len:Int, prob:Double):Unit = {
        if(len == length) {
          paths.get((startId, curId)) match {
            case Some(p) => paths.put((startId, curId), p + prob)  
            case None => paths.put((startId, curId), prob)
          }
        } else {
          val n = db.getNodeById(curId)
          for(x <- n.getRelationships(Direction.OUTGOING, probType).asScala) {
            val otherId = x.getOtherNode(n).getId
            traverse(startId, otherId, len + 1, prob * x.getProperty("p").asInstanceOf[Double])
          }
        }
      }
 
      tx.success
      Response.ok(compact(render(decompose(paths.map(m => MarkovResult(m._1._1, m._1._2, m._2))))), MediaType.APPLICATION_JSON).build()
    } catch {
      case e:Exception => Response.status(500).entity(e.getMessage).build()
    } finally {
      tx.close
    }
  }

}
