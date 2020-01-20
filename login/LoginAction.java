/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.cca.portal.archivos.web.actions;

import cl.cca.portal.archivos.ejb.beans.Comun;
import cl.cca.portal.archivos.ejb.beans.Login;
import cl.cca.portal.archivos.ejb.constantes.Constantes;
import cl.cca.portal.archivos.ejb.exceptions.BaseDatosException;
import cl.cca.portal.archivos.ejb.exceptions.AplicacionException;
import cl.cca.portal.archivos.ejb.exceptions.PasswordIncorrectaException;
import cl.cca.portal.archivos.ejb.exceptions.UsuarioBloqueadoException;
import cl.cca.portal.archivos.ejb.exceptions.UsuarioNoExisteException;
import cl.cca.portal.archivos.ejb.objects.AccesoVO;
import cl.cca.portal.archivos.ejb.objects.BuscadorVO;
import cl.cca.portal.archivos.ejb.objects.PerfilVO;
import cl.cca.portal.archivos.ejb.objects.UsuarioVO;
import cl.cca.portal.archivos.web.locator.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

/**
 *
 * @author Administrador
 */
public class LoginAction extends org.apache.struts.action.Action {

    private static final int UNO = 1;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        DynaActionForm formulario = new DynaActionForm();
        if(form instanceof DynaActionForm){
            formulario = (DynaActionForm) form;
        }
        String userAction = formulario.getString("accion");
        Logger.getLogger(LoginAction.class.getName()).log(Level.INFO, "LoginAction: {0}", userAction);
        switch (userAction) {
            case "ingresar":
                return doLogin(mapping, formulario, request);
            case "salir":
                return doLogout(mapping, formulario, request);
            case "olvido.pass.p1":
                return doOlvidoPasswordP1(mapping, formulario);
            case "olvido.pass.p2":
                return doOlvidoPasswordP2(mapping, formulario, request);
            case "cambio.pass":
                return doCambioPassword(mapping, formulario, request);
            case "index":
                return doIndex(mapping, formulario, request);
            case "error":
                return doIndexError(mapping, formulario, request);
            default:
                return doIndex(mapping, formulario, request);
        }
    }

    public ActionForward doIndex(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        formulario.initialize(mapping);
        HttpSession session = request.getSession();
        session.invalidate();
        return mapping.findForward("index");
    }

    public ActionForward doIndexError(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        formulario.initialize(mapping);
        HttpSession session = request.getSession();
        session.invalidate();
        return mapping.findForward("errormain");
    }
    
    private Login getLogin() throws NamingException {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        Login login = (Login) serviceLocator.getHome(ServiceLocator.LOGIN);
        return login;
    }

    public ActionForward doLogin(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        formulario.set("cambioOK", null);
        ActionMessages messages = new ActionMessages();
        ValidaLoginAction validador = new ValidaLoginAction();
        if (validador.validaFormIngreso(formulario, request) < 0) {
            return mapping.findForward("index");
        }
        BuscadorVO buscador = new BuscadorVO();
        UsuarioVO usuario;
        request.getSession(false);
        try {
            HttpSession session = request.getSession(false);
            if (!session.isNew()) {
                session.invalidate();
            }
            buscador.setLogin(formulario.getString("usuaRut"));
            buscador.setPalabraClav(formulario.getString("password"));
            buscador.setIp(request.getRemoteAddr());
            buscador.setNombreEquipo(request.getRemoteHost());
            Login autentifica = this.getLogin();
            usuario = autentifica.validarLogin(buscador);
            usuario.setIpAcceso(request.getRemoteAddr());
            usuario.setNombreMaquina(request.getRemoteHost());
            session = request.getSession(true);
            usuario.setSessionAcceso(session.getId());
            request.getSession().setAttribute("usuarioVO", usuario);
        } catch (AplicacionException | BaseDatosException eda) {
            ActionMessage errorMsg = new ActionMessage("error.APLICACION");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
            return mapping.findForward("index");
        } catch (UsuarioNoExisteException | UsuarioBloqueadoException | PasswordIncorrectaException une) {
            ActionMessage errorMsg = new ActionMessage("error.passwordIncorrecta");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
            return mapping.findForward("index");
        } catch (NamingException ex) {
            Logger.getLogger(LoginAction.class.getName()).log(Level.SEVERE, null, ex);
            ActionMessage errorMsg = new ActionMessage("error.APLICACION");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
            return mapping.findForward("index");
        }
        if (usuario.isCambioPas()) {
            return mapping.findForward("cambioPassword");
        }
        if (usuario.getListaPerfiles() != null && usuario.getListaPerfiles().size() > UNO) {
            return mapping.findForward("listaPerfiles");
        } else if (usuario.getListaPerfiles() != null && usuario.getListaPerfiles().size() == UNO) {
            return doFordwardPerfil(mapping, usuario.getListaPerfiles().get(0), usuario, request, formulario);
        } else {
            ActionMessage errorMsg = new ActionMessage("error.perfilNoExiste");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
            return mapping.findForward("index");
        }
    }

    private ActionForward doFordwardPerfil(ActionMapping mapping, PerfilVO perfil, UsuarioVO usuario, HttpServletRequest request, DynaActionForm formulario) {
        usuario.setIdPerfil(perfil.getId());
        usuario.setPerfil(perfil.getDescripcion());
        usuario.setPeusId(perfil.getIdPerfilUsuario());
        Logger.getLogger(LoginAction.class.getName()).log(Level.INFO, "Ingreso usuario: {0}. Perfil: {1}", new Object[]{usuario.getLogin(), usuario.getPerfil()});
        try {
            Login autentifica = this.getLogin();
            usuario = autentifica.ingresarAcceso(usuario);
            request.getSession().setAttribute("usuarioVO", usuario);
            this.setUltimosAccesosUsuario(request);
        } catch (NamingException | BaseDatosException | AplicacionException e) {
            Logger.getLogger(LoginAction.class.getName()).log(Level.WARNING, null, e);
        }
        switch (perfil.getId()) {
            case Constantes.ID_PERFIL_ADMINISTRADOR_CCA:
                return mapping.findForward("administradorCCA");
            case Constantes.ID_PERFIL_ADMINISTRADOR_BCO:
                return mapping.findForward("administradorBco");
            case Constantes.ID_PERFIL_OPERADOR_BCO:
                return mapping.findForward("operadorBco");
            case Constantes.ID_PERFIL_OPERADOR_CCA:
                return mapping.findForward("operadorCCA");
            case Constantes.ID_PERFIL_ADMINISTRADOR_SISTEMA:
                formulario.set("accion", "menu.admin.sist.inicio");
                return mapping.findForward("administradorSistema");
            case Constantes.ID_PERFIL_OSI:
                return mapping.findForward("osi");
            case Constantes.ID_PERFIL_GERENTE:
                return mapping.findForward("gerencia");
            case Constantes.ID_PERFIL_REPORTES:
                return mapping.findForward("reportes");
            default:
                break;
        }
        return mapping.findForward("index");
    }

    private void setUltimosAccesosUsuario(HttpServletRequest request) {
        List<AccesoVO> lista = new ArrayList<>();
        try {
            lista = getLogin().ultimosAccesosUsuario((UsuarioVO) request.getSession().getAttribute("usuarioVO"));
            if (lista.size() > 1) {
                AccesoVO ultimo = (AccesoVO) lista.get(0);
                request.getSession().setAttribute("ULTIMO_ACCESO", ultimo);
            }
        } catch (NamingException | BaseDatosException | AplicacionException e) {
            Logger.getLogger(LoginAction.class.getName()).log(Level.OFF, null, e);
        }
        request.getSession().setAttribute("ACCESOS", lista);
    }

    public ActionForward doLogout(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        formulario.initialize(mapping);
        ActionMessages messages = new ActionMessages();
        try {
            Login login = this.getLogin();
            UsuarioVO usua = (UsuarioVO) request.getSession().getAttribute("usuarioVO");
            if (usua != null && usua.getIdUsuario()> 0) {
                usua.setSalir(true);
                login.actualizarAccesoUsuario(usua, "N");
            }
            HttpSession session = request.getSession();
            session.invalidate();
        } catch (BaseDatosException | AplicacionException | NamingException t) {
            ActionMessage errorMsg = new ActionMessage("error.session.expirada");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
        }
        return mapping.findForward("index");
    }

    public ActionForward doOlvidoPasswordP1(ActionMapping mapping, DynaActionForm formulario) {
        formulario.initialize(mapping);
        return mapping.findForward("olvidoPasswordP1");
    }

    public ActionForward doOlvidoPasswordP2(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        ValidaLoginAction validador = new ValidaLoginAction();
        if (validador.validaFormOlvidoPass(formulario, request) < 0) {
            return mapping.findForward("olvidoPasswordP1");
        }
        BuscadorVO buscador = new BuscadorVO();
        buscador.setLogin(formulario.getString("usuaRut"));
        buscador.setIp(request.getRemoteAddr());
        buscador.setNombreEquipo(request.getRemoteHost());
        new ThreadEmail("Olvido Password", buscador).start();
        return mapping.findForward("index");
    }

    public ActionForward doCambioPassword(ActionMapping mapping, DynaActionForm formulario, HttpServletRequest request) {
        ActionMessages messages = new ActionMessages();
        ValidaComunAction validador = new ValidaComunAction();
        UsuarioVO usuario = getUsuarioSession(request);
        if (validador.validaCambioPassword(formulario, request, usuario) < 0) {
            return mapping.findForward("cambioPassword");
        }
        try {
            usuario.setNuevaClave(formulario.getString("password1"));
            Login ejb = this.getLogin();
            ejb.actualizarPasswordUsuario(usuario);
        } catch (NamingException | BaseDatosException | AplicacionException e) {
            ActionMessage errorMsg = new ActionMessage("error.APLICACION");
            messages.add("errorLogin", errorMsg);
            saveErrors(request, messages);
            return mapping.findForward("cambioPassword");
        }
        formulario.initialize(mapping);
        return mapping.findForward("cambioPasswordP2");
    }

    public static Comun getComun() throws NamingException {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        Comun login = (Comun) serviceLocator.getHome(ServiceLocator.COMUN);
        return login;
    }

    private static UsuarioVO getUsuarioSession(HttpServletRequest request){
        UsuarioVO u = new UsuarioVO();
        Object o = request.getSession().getAttribute("usuarioVO");
        if (o instanceof UsuarioVO) {
            u = (UsuarioVO) o;
        }        
        return u;
    }    
}

class ThreadEmail extends Thread {

    private final BuscadorVO buscador;

    ThreadEmail(String str, BuscadorVO buscador) {
        super(str);
        this.buscador = buscador;
    }

    private Login getLogin() throws NamingException {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        Login login = (Login) serviceLocator.getHome(ServiceLocator.LOGIN);
        return login;
    }

    @Override
    public void run() {
        try {
            Login ejb = getLogin();
            ejb.generaNuevoPasswordOlvido(buscador);
        } catch (NamingException ex) {
            Logger.getLogger(ThreadEmail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BaseDatosException | AplicacionException ex) {
            Logger.getLogger(ThreadEmail.class.getName()).log(Level.OFF, null, ex);
        }
    }
}
