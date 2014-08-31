/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.restapiv3.test;

/**
 *
 * @author matteo
 */
import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.utils.ThrowableExceptionMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public abstract class AbstractTest<Z> extends JerseyTest {

    private String path;
    private Z item;
    private GenericType<Z> singleType;
    private GenericType<List<Z>> listType;
    private String uuid;
    private MediaType representation;
    private API api;

    // Expect that subclasses implement this methods
    abstract void init() throws UriBuilderException, IllegalArgumentException;

    abstract void putModifications(Z orig);

    abstract void putAssertions(Z pre, Z post);

    abstract void getAssertions(Z obj);

    abstract void listAssertions(List<Z> list);

    abstract String getUuid(Z obj);

    @Override
    protected Application configure() {
        uuid = UUID.randomUUID().toString();
        System.out.println("DEBUG: I'm in configure of abstracttest");
        Injector injector = Guice.createInjector(new FreedomoticInjector());
        api = injector.getInstance(API.class);
        //        api = Freedomotic.INJECTOR.getInstance(API.class);
        System.out.println("DEBUG: Api reference in configure is " + api);
        init();
        representation = MediaType.APPLICATION_JSON_TYPE;

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig rc = new ResourceConfig().packages(RestAPIv3.JERSEY_RESOURCE_PKG);
        rc.registerClasses(JacksonFeature.class);
        //  rc.registerClasses(MoxyXmlFeature.class);
        rc.register(ThrowableExceptionMapper.class);
        return rc;
    }

    @After
    @Override
    public void tearDown() throws Exception {
        getApi().commands().clear();
        getApi().environments().clear();
        getApi().triggers().clear();
        getApi().reactions().clear();
        getApi().objects().clear();
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
        System.out.println("DEBUG: tear down");
    }

    @Test
    public void test() {
        Entity<Z> cmdEntity = Entity.entity(getItem(), getRepresentation());

        // POST
        final Response response = target(getPATH()).request().post(cmdEntity);
        assertEquals("POST response HTTP status code not as expected", Status.CREATED.getStatusCode(), response.getStatus());
        //System.out.println(resPOST.getLocation().toString());

        //GET list
        List<Z> cl = target(getPATH()).request(getRepresentation()).get(getListType());
        assertEquals("List size", 1, cl.size());
        listAssertions(cl);

        //GET single
        Z objPre = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).get(getSingleType());
        getAssertions(objPre);

        // PUT
        putModifications(objPre);
        Entity<Z> envEntityPut = Entity.entity(objPre, getRepresentation());
        Response resPUT = target(getPATH()).path(getUuid(getItem())).request().put(envEntityPut);
        assertEquals("PUT test", Status.OK.getStatusCode(), resPUT.getStatus());
        Z objPost = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).get(getSingleType());
        putAssertions(objPre, objPost);

        //COPY
        final Response resCOPY = target(getPATH()).path(getUuid(getItem())).path("/copy").request(getRepresentation()).post(null);
        assertEquals("COPY test", Status.CREATED.getStatusCode(), resCOPY.getStatus());
        cl = target(getPATH()).request().get(getListType());
        assertEquals("COPY - Size test", 2, cl.size());

        //DELETE
        Response resDELETE = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).delete();
        assertEquals("DELETE test", Status.OK.getStatusCode(), resDELETE.getStatus());
        cl = target(getPATH()).request().get(getListType());
        assertEquals("DELETE - Size test", 1, cl.size());
        Response postDELETE = target(getPATH()).path(getUuid(getItem())).request().get();
        assertEquals("DELETE - error searching deleted item", Status.NOT_FOUND.getStatusCode(), postDELETE.getStatus());

    }

    protected void initPath(Class res) {
        setPATH(UriBuilder.fromResource(res).build().toString());
    }

    protected void initPath(String path) {
        setPATH(path);
        //System.out.print("PATH: " + getPATH());
    }

    /**
     * @return the path
     */
    public String getPATH() {
        return path;
    }

    /**
     * @param PATH the path to set
     */
    public void setPATH(String PATH) {
        this.path = PATH;
    }

    /**
     * @return the item
     */
    public Z getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Z item) {
        this.item = item;
    }

    /**
     * @return the singleType
     */
    public GenericType<Z> getSingleType() {
        return singleType;
    }

    /**
     * @param singleType the singleType to set
     */
    public void setSingleType(GenericType<Z> singleType) {
        this.singleType = singleType;
    }

    /**
     * @return the listType
     */
    public GenericType<List<Z>> getListType() {
        return listType;
    }

    /**
     * @param listType the listType to set
     */
    public void setListType(GenericType<List<Z>> listType) {
        this.listType = listType;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the representation
     */
    public MediaType getRepresentation() {
        return representation;
    }

    /**
     * @param representation the representation to set
     */
    public void setRepresentation(MediaType representation) {
        this.representation = representation;
    }

    /**
     * @return the api
     */
    public API getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(API api) {
        this.api = api;
    }

}
