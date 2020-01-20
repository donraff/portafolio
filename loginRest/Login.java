/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.b2brk.web.rest.kcc.ws;

import com.b2brk.ejb.exception.*;
import com.b2brk.web.rest.kcc.objects.UsuarioVO;
import com.b2brk.web.rest.kcc.ws.dao.LoginTrackingDAO;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author rvallejos
 */
@Stateless
@Path("login")
public class Login {

    @GET
    @Produces({"application/json"})//@Produces({"application/xml", "application/json"})    
    //@Path("/login")
    public Response validaUsuario(@QueryParam("login") String login, @QueryParam("pwd") String pwd){
        UsuarioVO usuario = new UsuarioVO();
        LoginTrackingDAO dao = new LoginTrackingDAO();
        try{
            usuario = dao.verificaUsuario(login, pwd);
            dao.setDatosEmpresa(usuario);
            usuario.setEST("00");
            usuario.setMSG("Login OK");
        }catch(UsuarioNoExiste u){
            usuario.setEST("01");
            usuario.setMSG("Usuario No Existe");
        }catch(UsuarioBloqueado u){
            usuario.setEST("02");
            usuario.setMSG("Usuario Bloqueado");
        }catch(PasswordIncorrecta u){
            usuario.setEST("03");
            usuario.setMSG("Password Incorrecta");
        }catch(EmpresaBloqueada u){
            usuario.setEST("04");
            usuario.setMSG("Empresa Bloqueada");
        }catch(ErrorBaseDatos u){
            usuario.setEST("05");
            usuario.setMSG("Error Base Datos");
        }catch(ErrorDeAplicacion u){
            usuario.setEST("06");
            usuario.setMSG("Error De Aplicacion");
        }catch(Exception u){
            usuario.setEST("06");
            usuario.setMSG("Error De Aplicacion");
        }
/*      usuario.setIdUsua(11);
        usuario.setNombre("PERICO PEREZ");
        usuario.setIdEmpresa(22);
        usuario.setEmpresa("KCC");
        usuario.setIdPerfil(2);
        usuario.setPerfil("Operador Carga");
*/
        return Response.ok(usuario).build();
    }
/*
    @GET
    @Produces({"application/json"})//@Produces({"application/xml", "application/json"})    
    @Path("/listEmp")
    public Response listadoEmpresas(){
        List<CodigoDescripcionVO> lista = new ArrayList<CodigoDescripcionVO>();
        //return lista;
        CodigoDescripcionVO cod = new CodigoDescripcionVO();
        cod.setId(1);
        cod.setDesc("Empr 1");
        lista.add(cod);
        
        cod = new CodigoDescripcionVO();
        cod.setId(2);
        cod.setDesc("Empr 2");
        lista.add(cod);
        
        return Response.ok(lista).build();
    }
*/

}
