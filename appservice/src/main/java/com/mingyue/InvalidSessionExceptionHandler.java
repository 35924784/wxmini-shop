package com.mingyue;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

 
@Provider
public class InvalidSessionExceptionHandler implements ExceptionMapper<InvalidSessionException>
{
    @Override
    public Response toResponse(InvalidSessionException exception)
    {
        return Response.status(456).entity(exception.getMessage()).build(); 
    }  

}