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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.UnknownHostException;
import org.jaqpot.core.data.serialize.JacksonJSONSerializer;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class MongoDBEntityManagerTest {

    @Mock
    private JacksonJSONSerializer serializer;

    ObjectMapper mapper;
    Task taskPojo;
    String taskJSON;

    @InjectMocks
    private MongoDBEntityManager em;

    public MongoDBEntityManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        MongoClient mongoClient = new MongoClient();
        mongoClient.dropDatabase("test");

        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        taskPojo = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

        mapper = new ObjectMapper();
        taskJSON = mapper.writeValueAsString(taskPojo);

        MockitoAnnotations.initMocks(this);
        Mockito.when(serializer.write(Matchers.any())).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object obj = invocation.getArguments()[0];
                return mapper.writeValueAsString(obj);
            }
        });
        Mockito.when(serializer.parse(Matchers.anyString(), Matchers.any(Class.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String pojo = (String) invocation.getArguments()[0];
                Class clazz = (Class) invocation.getArguments()[1];
                return mapper.readValue(pojo, clazz);
            }
        });

        em.setDatabase("test");

    }

    @After
    public void tearDown() {
    }

    /**
     * Writes a task to mongodb and retrieves it by ID.
     *
     * @throws IOException
     */
    @Test
    public void testSaveTask() throws IOException {
        /* Persist entity using EntityManager */
        em.persist(taskPojo);

        //Now find the object in the database:
        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID

        // Now find it in the DB...
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        Task objFromDB = (Task) mapper.readValue(retrieved.toString(), Task.class);

        assertEquals(taskPojo, objFromDB);
        assertEquals("not the same ID", taskPojo.getId(), objFromDB.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), objFromDB.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), objFromDB.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), objFromDB.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), objFromDB.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), objFromDB.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), objFromDB.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), objFromDB.getMeta().getDescriptions());
    }

    @Test
    public void testFindTask() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBObject taskDBObj = (DBObject) JSON.parse(taskJSON);
        coll.insert(taskDBObj);

        Task foundTask = em.find(Task.class, taskPojo.getId());

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), foundTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());
    }

    @Test
    public void testMergeTask() throws UnknownHostException, IOException {
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBObject taskDBObj = (DBObject) JSON.parse(taskJSON);
        coll.insert(taskDBObj);

        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very cool task", "oh, and it's super useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        Task mergeTask = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        mergeTask.setCreatedBy("random-user@jaqpot.org");
        mergeTask.setPercentageCompleted(0.95f);
        mergeTask.setDuration(1534l);
        mergeTask.setMeta(meta);
        mergeTask.setHttpStatus(202);
        mergeTask.setStatus(Task.Status.RUNNING);

        Task oldTask = em.merge(mergeTask);

        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        Task objFromDB = (Task) mapper.readValue(retrieved.toString(), Task.class);

        assertEquals(mergeTask, objFromDB);
        assertEquals("not the same ID", mergeTask.getId(), objFromDB.getId());
        assertEquals("not the same createdBy", mergeTask.getCreatedBy(), objFromDB.getCreatedBy());
        assertEquals("not the same percentageComplete", mergeTask.getPercentageCompleted(), objFromDB.getPercentageCompleted());
        assertEquals("not the same duration", mergeTask.getDuration(), objFromDB.getDuration());
        assertEquals("not the same HTTP status", mergeTask.getHttpStatus(), objFromDB.getHttpStatus());
        assertEquals("not the same status", mergeTask.getStatus(), objFromDB.getStatus());
        assertEquals("not the same comments", mergeTask.getMeta().getComments(), objFromDB.getMeta().getComments());
        assertEquals("not the same descriptions", mergeTask.getMeta().getDescriptions(), objFromDB.getMeta().getDescriptions());

        assertEquals(oldTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), oldTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), oldTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), oldTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), oldTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), oldTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), oldTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), oldTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), oldTask.getMeta().getDescriptions());

    }

}
