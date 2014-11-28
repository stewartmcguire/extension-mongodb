package org.opencfmlfoundation.mongodb;

import java.util.Iterator;
import java.util.Map.Entry;

import org.opencfmlfoundation.mongodb.support.DBCollectionImplSupport;
import org.opencfmlfoundation.mongodb.util.print;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import railo.runtime.PageContext;
import railo.runtime.dump.DumpData;
import railo.runtime.dump.DumpProperties;
import railo.runtime.dump.DumpTable;
import railo.runtime.exp.PageException;
import railo.runtime.type.Array;
import railo.runtime.type.Collection;
import railo.runtime.type.Collection.Key;
import railo.runtime.type.Struct;
import railo.runtime.type.dt.DateTime;

public class DBCollectionImpl extends DBCollectionImplSupport {

	private DBCollection coll;

	public DBCollectionImpl(DBCollection coll) {
		this.coll=coll;
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		throw new UnsupportedOperationException("there are no properties for this DBCollection!");
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		throw new UnsupportedOperationException("there are no properties for this DBCollection!");
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		throw new UnsupportedOperationException("you cannot set a property to DBCollection!");
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		throw new UnsupportedOperationException("you cannot set a property to DBCollection!");
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {

		// aggregate
		if(methodName.equals("aggregate")) {
			int len=checkArgLength("aggregate",args,1,-1); // no length limitation
			DBObject firstArg;
			DBObject[] addArgs;
			// Array
			if(len==1 && decision.isArray(args[0])) {
				Array arr = caster.toArray(args[0]);
				if(arr.size()==0)
					throw exp.createApplicationException("the array passed to the function aggregate needs at least 1 element");
				
				Iterator<Object> it = arr.valueIterator();
				firstArg=toDBObject(it.next());
				addArgs=new DBObject[arr.size()-1];
				int i=0;
				while(it.hasNext()){
					addArgs[i++]=toDBObject(it.next());
				}
			}
			else {
				firstArg=toDBObject(args[0]);
				addArgs=new DBObject[len-1];
				for(int i=1;i<len;i++){
					addArgs[i-1]=toDBObject(args[i]);
				}
			}
			return toCFML(coll.aggregate(firstArg, addArgs));
		}
		// dataSize
		if(methodName.equals("dataSize")) {
			checkArgLength("dataSize",args,0,0);
			return toCFML(coll.getStats().get("size"));
		}
		
		// distinct
		if(methodName.equals("distinct")) {
			int len=checkArgLength("distinct",args,1,2);
			if(len==1){
				return toCFML(coll.distinct(
					caster.toString(args[0])
				));
			}
			else if(len==2){
				return toCFML(coll.distinct(
					caster.toString(args[0]),
					toDBObject(args[1])
				));
			}
		}
		// drop
		if(methodName.equals("drop")) {
			checkArgLength("drop",args,0,0);
			coll.drop();
			return null;
		}
		
		// dropIndex
		if(methodName.equals("dropIndex")) {
			checkArgLength("dropIndex",args,1,1);
			DBObject dbo = toDBObject(args[0], null);
			if(dbo!=null) coll.dropIndex(dbo);
			else coll.dropIndex(caster.toString(args[0]));
			
			return null;
		}
		// dropIndexes
		if(methodName.equals("dropIndexes")) {
			int len=checkArgLength("dropIndexes",args,0,1);
			if(len==0){
				coll.dropIndexes();
				return null;
			}
			else if(len==1){
				coll.dropIndexes(caster.toString(args[0]));
				return null;
			}
		}
		
		// ensureIndex
		if(methodName.equals("ensureIndex")) {
			int len=checkArgLength("ensureIndex",args,1,3);
			if(len==1){
				DBObject dbo = toDBObject(args[0], null);
				if(dbo!=null) coll.ensureIndex(dbo);
				else coll.ensureIndex(caster.toString(args[0]));
				return null;
			}
			if(len==2){
				DBObject p1 = toDBObject(args[0]);
				DBObject p2 = toDBObject(args[1], null);
				if(p2!=null) coll.ensureIndex(p1,p2);
				else coll.ensureIndex(p1,caster.toString(args[1]));
				return null;
			}
			else if(len==3){
				coll.ensureIndex(
						toDBObject(args[0]),
						caster.toString(args[1]),
						caster.toBooleanValue(args[2])
				);
				return null;
			}
		}
		
		// getStats
		if(methodName.equals("getStats") || methodName.equals("stats")) {
			checkArgLength("getStats",args,0,0);
			return toCFML(coll.getStats());
		}
		
		// getIndexes
		if(methodName.equals("getIndexes") || methodName.equals("getIndexInfo")) {
			checkArgLength(methodName.getString(),args,0,0);
			return toCFML(coll.getIndexInfo());
		}

		// find
		if(methodName.equals("find")) {
			int len=checkArgLength("find",args,0,3);
			DBCursor cursor=null;
			if(len==0) {
				cursor=coll.find();
			}
			else if(len==1){
				cursor=coll.find(
					toDBObject(args[0])
				);
			}
			else if(len==2){
				cursor=coll.find(
					toDBObject(args[0]),
					toDBObject(args[1])
				);
			}
			else if(len==3){
				cursor=coll.find(
					toDBObject(args[0]),
					toDBObject(args[1])
				).skip(caster.toIntValue(args[2]));
			}
			
			return toCFML(cursor);
		}
		// findOne
		else if(methodName.equals("findOne")) {
			int len=checkArgLength("findOne",args,0,3);
			DBObject obj=null;
			if(len==0) {
				obj=coll.findOne();
			}
			else if(len==1){
				DBObject arg1 = toDBObject(args[0],null);
				if(arg1!=null)obj=coll.findOne(arg1);
				else obj=coll.findOne(args[0]);
				
			}
			else if(len==2){
				DBObject arg1 = toDBObject(args[0],null);
				if(arg1!=null) obj=coll.findOne(arg1,toDBObject(args[1]));
				else obj=coll.findOne(args[0],toDBObject(args[1]));
			}
			else if(len==3){
				obj=coll.findOne(
					toDBObject(args[0]),
					toDBObject(args[1]),
					toDBObject(args[2])
				);
			}
			return toCFML(obj);
		}
		// findAndModify
		if(methodName.equals("findAndModify")) {
			int len=checkArgLength("findAndModify",args,2,3);
			DBObject obj=null;
			if(len==2){
				obj=coll.findAndModify(
					toDBObject(args[0]),
					toDBObject(args[1])
				);
			}
			if(len==3){
				obj=coll.findAndModify(
					toDBObject(args[0]),
					toDBObject(args[1]),
					toDBObject(args[2])
				);
			}
			// TODO more options
			
			return toCFML(obj);
		}

		//group
		if(methodName.equals("group")) {
			int len=checkArgLength("group",args,1,1);
			if(len==1){
				return toCFML(coll.group(
					toDBObject(args[0])
				));
			}
		}
		
		// insert
		if(methodName.equals("insert")) {
			checkArgLength("insert",args,1,1);
			return toCFML(coll.insert(
					toDBObjectArray(args[0]))
				);
		}

		//mapReduce
		if(methodName.equals("mapReduce")) {
			int len=checkArgLength("mapReduce",args,1,1);
			if(len==1){
				return toCFML(coll.mapReduce(
					toDBObject(args[0])
				));
			}
		}
		
		// reIndex
		if(methodName.equals("reIndex") || methodName.equals("resetIndexCache")) {
			checkArgLength("resetIndexCache",args,0,0);
			coll.resetIndexCache();
			return null;
		}
		
		// remove
		if(methodName.equals("remove")) {
			checkArgLength("remove",args,1,1);
			return toCFML(coll.remove(toDBObject(args[0])));
			
		}
		
		// rename
		if(methodName.equals("rename") || methodName.equals("renameCollection")) {
			int len=checkArgLength(methodName.getString(),args,1,2);
			if(len==1){
				return toCFML(coll.rename(
					caster.toString(args[0])
				));
			}
			else if(len==2){
				return toCFML(coll.rename(
						caster.toString(args[0]),
						caster.toBooleanValue(args[1])
					));
			}
		}
		
		// save
		if(methodName.equals("save")) {
			checkArgLength("save",args,1,1);
			return toCFML(coll.save(
					toDBObject(args[0]))
				);
		}

		// storageSize
		if(methodName.equals("storageSize")) {
			checkArgLength("storageSize",args,0,0);
			return toCFML(coll.getStats().get("storageSize"));
		}

		// totalIndexSize
		if(methodName.equals("totalIndexSize")) {
			checkArgLength("totalIndexSize",args,0,0);
			return toCFML(coll.getStats().get("totalIndexSize"));
		}
		
		// update
		if(methodName.equals("update")) {
			int len = checkArgLength("update",args,2,4);
			if(len==2){
				return toCFML(coll.update(
					toDBObject(args[0]),
					toDBObject(args[1])
				));
			}
			else if(len==3){
				return toCFML(coll.update(
					toDBObject(args[0]),
					toDBObject(args[1]),
					caster.toBooleanValue(args[2]),
					false
				));
			}
			else if(len==4){
				return toCFML(coll.update(
					toDBObject(args[0]),
					toDBObject(args[1]),
					caster.toBooleanValue(args[2]),
					caster.toBooleanValue(args[3])
				));
			}
		}
		
		
		String functionNames = "aggregate,dataSize,distinct,drop,dropIndex,dropIndexes,ensureIndex,stats,getIndexes,find,findOne,findAndModify," +
		"group,insert,mapReduce,reIndex,remove,rename,save,storageSize,totalIndexSize,update";
		
		throw exp.createApplicationException("function "+methodName+" does not exist existing functions are ["+functionNames+"]");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		throw new UnsupportedOperationException("named arguments are not supported yet!");
	}


	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DBCursor cursor = coll.find();
		Iterator<DBObject> it = cursor.iterator();
		DumpTable table = new DumpTable("struct","#339933","#8e714e","#000000");
		table.setTitle("DBCollection");
		
		maxlevel--;
		DBObject obj;
		while(it.hasNext()) {
			obj = it.next();
			table.appendRow(0,
					__toDumpData(toCFML(obj), pageContext,maxlevel,dp)
				);
		}
		return table;
	}

	public DBCollection getDBCollection() {
		return coll;
	}

}
