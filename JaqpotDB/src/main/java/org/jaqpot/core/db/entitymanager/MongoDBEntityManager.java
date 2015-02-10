/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.db.entitymanager;

import com.mongodb.BasicDBObject;
import org.jaqpot.core.annotations.MongoDB;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import org.jaqpot.core.data.serialize.EntityJSONSerializer;
import org.reflections.Reflections;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MongoDB
public class MongoDBEntityManager implements JaqpotEntityManager {

    private static final Logger LOG = Logger.getLogger(JaqpotEntityManager.class.getName());

    @Inject
    EntityJSONSerializer serializer;

    private MongoClient mongoClient;
    private String database;

    private static final Map<Class, String> collectionNames;

    static {
        collectionNames = new HashMap<>();
        try {

            Reflections reflections = new Reflections("org.jaqpot.core.model");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);            
            for (Class c : annotatedClasses) {
                Annotation xmlRootElement = c.getAnnotation(XmlRootElement.class);
                String value = (String) xmlRootElement.annotationType().getMethod("name").invoke(xmlRootElement);
                collectionNames.put(c, !value.equals("##default") ? value : c.getSimpleName());
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            String errorMessage = "Improper data model - all elementary POJOs ###MUST### be annotated with @XmlRootElement!";
            LOG.log(Level.SEVERE, errorMessage, ex);
            Logger.getLogger(MongoDBEntityManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex); // terrible case: No @XmlRootElement annotation in our data model! 
        }
    }

    public MongoDBEntityManager() {
        // Here construct the mongo client object...
        try {
            mongoClient = new MongoClient();
        } catch (UnknownHostException ex) {
            String errorMessage = "JaqpotEntityManager could not be crated properly. Please check your database configuration settings.";
            LOG.log(Level.SEVERE, errorMessage, ex);
            // this is a terribly bad error - throw a RuntimeException (this shouldn't ever happpen!!!)
            throw new RuntimeException(errorMessage);
        }

    }

    @Override
    public void persist(Object entity) {
        DB db = mongoClient.getDB(database);
        String entityJSON = serializer.write(entity);
        DBObject taskDBObj = (DBObject) JSON.parse(entityJSON);
        DBCollection collection = db.getCollection(collectionNames.get(entity.getClass()));
        collection.insert(taskDBObj);
    }

    @Override
    public <T> T merge(T entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        DB db = mongoClient.getDB(database);
        BasicDBObject query = new BasicDBObject("_id", primaryKey);
        DBCollection coll = db.getCollection(collectionNames.get(entityClass));
        DBCursor cursor = coll.find(query);
        DBObject retrieved = cursor.one();
        System.out.println(retrieved.toString());
        return serializer.parse(retrieved.toString(), entityClass);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}
