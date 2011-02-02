package org.tuitman.ajaxframe.mongo;

import net.liftweb.json.Formats
import net.liftweb.json.JsonAST.JObject
import scala.reflect.Manifest
import org.bson.types.ObjectId
import com.mongodb._;
import java.util.UUID;

import net.liftweb.mongodb.JObjectParser;
import net.liftweb.mongodb._;
//import net.liftweb.mongodb.Serializers;
import org.tuitman.ajaxframe.JsonExtract;

/*

creates a mongodocument from a class.

*/
trait MongoDocument[BaseDocument] {
	self: BaseDocument =>
	
	def _id : Any
	
	def meta: MongoDocumentCustomMeta[BaseDocument]
	
	def delete = {
		meta.delete("_id", _id)
	}
	
	def save = {
		meta.save(this)
		
	}

  	def getRef: Option[MongoRef] = _id match {
    	case oid: ObjectId => Some(MongoRef(meta.collectionName, oid))
        case _ => None
  	}

}


trait MongoDocumentCustomMeta[BaseDocument] extends MongoMeta[BaseDocument]{
	
	def clzz : Class[_];
	
	def create(dbo : DBObject) : BaseDocument = {
		JsonExtract.extract(JObjectParser.serialize(dbo).asInstanceOf[JObject],clzz).asInstanceOf[BaseDocument];
	}
	
  /**
  * Find a single row by a qry, using a DBObject.
  */
  def find(qry: DBObject): Option[BaseDocument] = {
    MongoDB.useCollection(mongoIdentifier, collectionName) ( coll =>
      coll.findOne(qry) match {
        case null => None
        case dbo => {
          Some(create(dbo))
        }
      }
    )
  }

  /**
  * Find a single document by _id using a String.
  */
  def find(s: String): Option[BaseDocument] =
    if (ObjectId.isValid(s))
      find(new BasicDBObject("_id", new ObjectId(s)))
    else
      find(new BasicDBObject("_id", s))

  /**
  * Find a single document by _id using an ObjectId.
  */
  def find(oid: ObjectId): Option[BaseDocument] = find(new BasicDBObject("_id", oid))

  /**
  * Find a single document by _id using a UUID.
  */
  def find(uuid: UUID): Option[BaseDocument] = find(new BasicDBObject("_id", uuid))

  /**
  * Find a single document by a qry using String, Any inputs
  */
  def find(k: String, v: Any): Option[BaseDocument] = find(new BasicDBObject(k, v))

  /**
  * Find a single document by a qry using a json query
  */
  def find(json: JObject): Option[BaseDocument] = find(JObjectParser.parse(json))

  /**
  * Find all documents in this collection
  */
  def findAll: List[BaseDocument] = {
    import scala.collection.JavaConversions._

    MongoDB.useCollection(mongoIdentifier, collectionName)(coll => {
      /** Mongo Cursors are both Iterable and Iterator, 
       * so we need to reduce ambiguity for implicits 
       */
      (coll.find: Iterator[DBObject]).map(create).toList
    })
  }

  /**
  * Find all documents using a DBObject query.
  */
  def findAll(qry: DBObject, sort: Option[DBObject], opts: FindOption*): List[BaseDocument] = {
    import scala.collection.JavaConversions._

    val findOpts = opts.toList

    MongoDB.useCollection(mongoIdentifier, collectionName) ( coll => {
      val cur = coll.find(qry).limit(
        findOpts.find(_.isInstanceOf[Limit]).map(x => x.value).getOrElse(0)
      ).skip(
        findOpts.find(_.isInstanceOf[Skip]).map(x => x.value).getOrElse(0)
      )
      sort.foreach( s => cur.sort(s))
      /** Mongo Cursors are both Iterable and Iterator, 
       * so we need to reduce ambiguity for implicits 
       */
      (cur: Iterator[DBObject]).map(create).toList
    })
  }

  /**
  * Find all documents using a DBObject query.
  */
  def findAll(qry: DBObject, opts: FindOption*): List[BaseDocument] =
    findAll(qry, None, opts :_*)

  /**
  * Find all documents using a DBObject query with sort
  */
  def findAll(qry: DBObject, sort: DBObject, opts: FindOption*): List[BaseDocument] =
    findAll(qry, Some(sort), opts :_*)

  /**
  * Find all documents using a JObject query
  */
  def findAll(qry: JObject, opts: FindOption*): List[BaseDocument] =
    findAll(JObjectParser.parse(qry), None, opts :_*)

  /**
  * Find all documents using a JObject query with sort
  */
  def findAll(qry: JObject, sort: JObject, opts: FindOption*): List[BaseDocument] =
    findAll(JObjectParser.parse(qry), Some(JObjectParser.parse(sort)), opts :_*)

  /**
  * Find all documents using a k, v query
  */
  def findAll(k: String, o: Any, opts: FindOption*): List[BaseDocument] =
    findAll(new BasicDBObject(k, o), None, opts :_*)

  /**
  * Find all documents using a k, v query with JObject sort
  */
  def findAll(k: String, o: Any, sort: JObject, opts: FindOption*): List[BaseDocument] =
    findAll(new BasicDBObject(k, o), Some(JObjectParser.parse(sort)), opts :_*)

  /*
  * Save a document to the db
  */
  def save(in: BaseDocument) {
    MongoDB.use(mongoIdentifier) ( db => {
      save(in, db)
    })
  }

  /*
  * Save a document to the db using the given Mongo instance
  */
  def save(in: BaseDocument, db: DB) {
    db.getCollection(collectionName).save(JObjectParser.parse(toJObject(in)))
  }

  /*
  * Update document with a JObject query using the given Mongo instance
  */
  def update(qry: JObject, newbd: BaseDocument, db: DB, opts: UpdateOption*) {
    update(qry, toJObject(newbd), db, opts :_*)
  }

  /*
  * Update document with a JObject query
  */
  def update(qry: JObject, newbd: BaseDocument, opts: UpdateOption*) {
    MongoDB.use(mongoIdentifier) ( db => {
      update(qry, newbd, db, opts :_*)
    })
  }


  def toJObject(in: BaseDocument)(implicit formats: Formats): JObject =
    net.liftweb.json.Extraction.decompose(in)(formats).asInstanceOf[JObject]

	/*
	* Case class for a db reference (foreign key). To be used in a JsonObject
	* ref = collection name, id is the value of the reference
	* Only works with ObjectIds.
	*/
	case class MongoRef(ref: String, id: ObjectId)

}