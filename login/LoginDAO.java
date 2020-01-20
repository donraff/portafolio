/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.cca.portal.archivos.ejb.daos;

import cl.cca.portal.archivos.ejb.constantes.Constantes;
import cl.cca.portal.archivos.ejb.exceptions.BaseDatosException;
import cl.cca.portal.archivos.ejb.exceptions.AplicacionException;
import cl.cca.portal.archivos.ejb.objects.AccesoVO;
import cl.cca.portal.archivos.ejb.objects.BuscadorVO;
import cl.cca.portal.archivos.ejb.objects.CodigoDescripcionVO;
import cl.cca.portal.archivos.ejb.objects.PerfilVO;
import cl.cca.portal.archivos.ejb.objects.UsuarioVO;
import cl.cca.portal.archivos.ejb.utils.EncriptaPassword;
import cl.cca.portal.archivos.ejb.utils.Utilitario;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrador
 */
public class LoginDAO extends ComunDAO {

    /**
     *
     * @param buscador
     * @return
     * @throws BaseDatosException
     */
    public UsuarioVO verificaUsuario(BuscadorVO buscador) throws BaseDatosException {
        UsuarioVO usuario = new UsuarioVO();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String select = "select u.[USUA_ID]"
                    + ",u.[USUA_ESUS_ID] "
                    + ",u.[USUA_LOGIN] "
                    + ",u.[USUA_BANC_ID] "
                    + ",u.[USUA_RUT] "
                    + ",u.[USUA_DV] "
                    + ",u.[USUA_PASSWORD] "
                    + ",u.[USUA_NOMBRE] "
                    + ",u.[USUA_AP_PATERNO] "
                    + ",u.[USUA_AP_MATERNO] "
                    + ",u.[USUA_EMAIL] "
                    + ",u.[USUA_CAMBIO_PWD] "
                    + ",u.[USUA_REINTENTOS] "
                    + ", (CONVERT(VARCHAR(10), GETDATE(), 103) + ' ' + CONVERT(VARCHAR(5), GETDATE(), 108)) AS FECHA "
                    + ", b.BANC_ID, b.BANC_DESCRIPCION "
                    + ",DATEDIFF(day,ISNULL(dbo.FNC_UltimaPasswordUsua(USUA_ID), [USUA_CREACION]), GETDATE()) as DIAS_ULTIMO_ACCESO "
                    + " FROM TB_USUARIO u, TB_BANCO b "
                    + " where u.USUA_BANC_ID = b.BANC_ID "
                    + " and u.USUA_LOGIN =  ? ";

            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setString(1, buscador.getLogin());
            rs = ps.executeQuery();
            if (rs.next()) {
                usuario.setId(rs.getInt("USUA_ID"));
                usuario.setLogin(rs.getString("USUA_LOGIN"));
                usuario.setNombre(rs.getString("USUA_NOMBRE").trim());
                usuario.setApellidoPaterno(rs.getString("usua_ap_paterno").trim());
                if (rs.getString("usua_ap_materno") != null) {
                    usuario.setApellidoMaterno(rs.getString("usua_ap_materno").trim());
                }
                String nombreFull = usuario.getNombre();
                if (usuario.getApellidoPaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoPaterno();
                }
                if (usuario.getApellidoMaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoMaterno();
                }
                usuario.setNombreFull(nombreFull);

                usuario.setRut(rs.getInt("USUA_RUT"));
                usuario.setDv(rs.getString("USUA_DV").trim());
                usuario.setRutFormateado(Utilitario.formatearRUT(usuario.getRut() + usuario.getDv()));

                usuario.setLogin(buscador.getLogin());
                usuario.setClave(rs.getString("usua_password"));
                usuario.setIdEstadoUsuario(rs.getInt("USUA_ESUS_ID"));
                usuario.setReintentos(rs.getInt("USUA_REINTENTOS"));
                if (rs.getInt("USUA_ESUS_ID") == Constantes.ESTADO_USUARIO_BLOQUEADO_ID) {
                    usuario.setBloqueado(true);
                } else {
                    usuario.setBloqueado(false);
                }
                usuario.setSfechaHoraIngreso(rs.getString("fecha"));

                if (rs.getString("USUA_CAMBIO_PWD") != null && rs.getString("USUA_CAMBIO_PWD").equals("S")) {
                    usuario.setCambioPas(true);
                }
                usuario.setIdInstitucion(rs.getInt("BANC_ID"));
                usuario.setIdInstitucionSTR(rs.getString("BANC_DESCRIPCION"));
                usuario.setEmail(rs.getString("USUA_EMAIL"));
                usuario.setDiasAcceso(rs.getInt("DIAS_ULTIMO_ACCESO"));
                usuario.setDiasExpirarPass(Constantes.PASSWD_DURACION - usuario.getDiasAcceso());
            }
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "verificaUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
        return usuario;
    }

    /**
     *
     * @param usuario
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    public void listaPerfiles(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String select = "select pu.peus_id "
                    + ", peus_perf_id "
                    + ", pu.peus_habilitado "
                    + ", p.perf_descripcion "
                    + " from "
                    + "tb_perfil_usuario pu, "
                    + "tb_perfil p "
                    + " where p.perf_id = pu.peus_perf_id "
                    + " and pu.peus_usua_id = ? "
                    + " and peus_habilitado = ? "
                    + " order by p.perf_descripcion ";
            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setLong(1, usuario.getIdUsuario());
            ps.setString(2, Constantes.SI);
            rs = ps.executeQuery();
            ArrayList<PerfilVO> lista = new ArrayList<>();
            while (rs.next()) {
                PerfilVO perfil = new PerfilVO();
                perfil.setId(rs.getInt("peus_perf_id"));
                perfil.setIdPerfilUsuario(rs.getInt("peus_id"));
                perfil.setDescripcion(rs.getString("perf_descripcion").trim());
                perfil.setHabilitado(true);
                lista.add(perfil);
            }
            usuario.setListaPerfiles(lista);
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
    }

    /**
     *
     * @param usuario
     * @return
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    public AccesoVO ultimoAccesoUsuario(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        AccesoVO acceso = new AccesoVO();
        try {
            String select = "SELECT ( CONVERT(VARCHAR(10), ACCE_FECHA_INICIO, 103) + ' ' + CONVERT(VARCHAR(8), ACCE_FECHA_INICIO, 108) ) as inicio "
                    + ", ( CONVERT(VARCHAR(10), ACCE_FECHA_ULTIMA_ACCION, 103) + ' ' + CONVERT(VARCHAR(8), ACCE_FECHA_ULTIMA_ACCION, 108) ) as fin "
                    + " FROM TB_ACCESOS "
                    + " WHERE  ACCE_PEUS_ID = ? ";
            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setLong(1, usuario.getPeusId());
            rs = ps.executeQuery();
            if (rs.next()) {
                acceso.setFechaInicio(rs.getString("inicio").trim());
                acceso.setFechaFin(rs.getString("fin").trim());
            }
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
        return acceso;
    }

    /**
     *
     * @param usuario
     * @return
     * @throws BaseDatosException
     */
    public UsuarioVO ingresarAcceso(UsuarioVO usuario) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            String updateSQL = "insert into TB_ACCESOS ("
                    + "ACCE_ID "
                    + ", ACCE_PEUS_ID "
                    + ", ACCE_IP_CLIENTE "
                    + ", ACCE_SESSION_ID "
                    + ", ACCE_FLAG_ACTIVA "
                    + ", ACCE_FECHA_ULTIMA_ACCION "
                    + ", ACCE_FECHA_INICIO "
                    + ", ACCE_CREACION "
                    + ", ACCE_CREADOR "
                    + ") values(?,?,?,?,?,getdate(),getdate(),getdate(),?) ";
            long id = this.obtenerNextval("TB_ACCESOS");
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);
            ps.setLong(1, id);
            ps.setLong(2, usuario.getPeusId());
            ps.setString(3, usuario.getIpAcceso());
            ps.setString(4, usuario.getSessionAcceso());
            ps.setString(5, "S");
            ps.setLong(6, usuario.getIdUsuario());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
            usuario.setIdAcceso(id);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }
        return usuario;
    }

    /**
     *
     * @param usuario
     * @param estado
     * @throws BaseDatosException
     */
    public void actualizarAccesoUsuario(UsuarioVO usuario, String estado) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            String updateSQL = "UPDATE TB_ACCESOS SET "
                    + " ACCE_FECHA_ULTIMA_ACCION = GETDATE() "
                    + ", ACCE_MODIFICACION = GETDATE() "
                    + ", ACCE_MODIFICADOR = ? "
                    + ", ACCE_FLAG_ACTIVA = ? "
                    + " WHERE  ACCE_ID = ? ";
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);
            ps.setLong(1, usuario.getIdUsuario());
            ps.setString(2, estado);
            ps.setLong(3, usuario.getIdAcceso());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }
    }

    /**
     *
     * @param usuario
     * @return
     * @throws BaseDatosException
     */
    public ArrayList<AccesoVO> ultimosAccesosUsuario(UsuarioVO usuario) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AccesoVO> listado = new ArrayList<>();
        try {
            String select = "SELECT TOP " + Constantes.CANTIDAD_ULTIMOS_ACCESOS + " [ACCE_ID] "
                    + "	, (CONVERT(VARCHAR(10), ACCE_FECHA_INICIO, 103) + ' ' + CONVERT(VARCHAR(8), ACCE_FECHA_INICIO, 108)) AS inicio "
                    + "	, (CONVERT(VARCHAR(10), ACCE_FECHA_ULTIMA_ACCION, 103) + ' ' + CONVERT(VARCHAR(8), ACCE_FECHA_ULTIMA_ACCION, 108)) AS fin "
                    + " FROM [dbo].[TB_ACCESOS] a "
                    + "	, [dbo].[TB_PERFIL_USUARIO] pu "
                    + " WHERE a.ACCE_PEUS_ID = pu.PEUS_ID "
                    + "	AND pu.PEUS_USUA_ID = ? "
                    + "	AND pu.PEUS_PERF_ID = ? "
                    + " ORDER BY [ACCE_ID] DESC";

            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setLong(1, usuario.getIdUsuario());
            ps.setInt(2, usuario.getIdPerfil());

            rs = ps.executeQuery();
            while (rs.next()) {
                AccesoVO acceso = new AccesoVO();
                acceso.setFechaInicio(rs.getString("inicio").trim());
                acceso.setFechaFin(rs.getString("fin").trim());
                listado.add(acceso);
            }
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
        return listado;
    }

    /**
     *
     * @param usuario
     * @param idModificador
     * @param cambioPWD
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    public void actualizarPasswordUsuario(UsuarioVO usuario, long idModificador, boolean cambioPWD) throws BaseDatosException, AplicacionException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            String updateSQL = "update TB_USUARIO set "
                    + " USUA_PASSWORD = ?  "//1
                    + ", usua_cambio_pwd = ? "//2
                    + ", USUA_MODIFICACION = GETDATE() "
                    + ", USUA_MODIFICADOR = ? "//3
                    + ", USUA_ESUS_ID = ? "//4
                    + " WHERE  USUA_ID = ? ";//5
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);
            String newPass = EncriptaPassword.getEncodedPassword(usuario.getNuevaClave());
            ps.setString(1, newPass);
            if (cambioPWD) {
                ps.setString(2, "S");
            } else {
                ps.setString(2, "N");
            }
            ps.setLong(3, idModificador);
            ps.setInt(4, Constantes.ESTADO_USUARIO_HABILITADO_ID);
            ps.setLong(5, usuario.getIdUsuario());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);

            if (!cambioPWD) {
                ingresarHistoricoPassword(usuario, newPass);
            }
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "actualizarPasswordUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.INFO, "actualizarPasswordUsuario", ex);
            throw new AplicacionException("ERROR_DE_APLICACION");
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }
    }

    /**
     *
     * @param usuario
     * @throws BaseDatosException
     */
    public void descuentaIngresos(UsuarioVO usuario) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            int valor = usuario.getReintentos() - 1;
            String updateSQL = "UPDATE TB_USUARIO SET "
                    + " [USUA_REINTENTOS] = ?  "
                    + ", [USUA_ESUS_ID] = ? "
                    + ", [USUA_MODIFICACION] = GETDATE() "
                    + ", [USUA_MODIFICADOR] = ? "
                    + " WHERE [USUA_ID] = ? ";
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);

            ps.setInt(1, valor);
            if (valor == 0) {
                ps.setInt(2, Constantes.ESTADO_USUARIO_BLOQUEADO_ID);
            } else {
                ps.setInt(2, usuario.getIdEstadoUsuario());
            }
            ps.setLong(3, usuario.getIdUsuario());
            ps.setLong(4, usuario.getIdUsuario());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "actualizarPasswordUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }
    }

    /**
     *
     * @param usuario
     * @throws BaseDatosException
     */
    public void reseteaIngresos(UsuarioVO usuario) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            String updateSQL = "UPDATE TB_USUARIO SET "
                    + " [USUA_REINTENTOS] = ?  "
                    + ", [USUA_MODIFICACION] = GETDATE() "
                    + ", [USUA_MODIFICADOR] = ? "
                    + " WHERE [USUA_ID] = ? ";
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);

            ps.setInt(1, Constantes.CANTIDAD_REINTENTOS);
            ps.setLong(2, usuario.getIdUsuario());
            ps.setLong(3, usuario.getIdUsuario());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "actualizarPasswordUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }
    }

    /**
     *
     * @param numRut
     * @return
     * @throws BaseDatosException
     */
    public UsuarioVO obtieneUsuarioResetPassword(int numRut) throws BaseDatosException {
        UsuarioVO usuario = new UsuarioVO();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String select = "select u.[USUA_ID], USUA_LOGIN "
                    + ",u.[USUA_ESUS_ID] "
                    + ",u.[USUA_NOMBRE] "
                    + ",u.[USUA_AP_PATERNO] "
                    + ",u.[USUA_AP_MATERNO] "
                    + ",u.[USUA_EMAIL] "
                    + " FROM TB_USUARIO u "
                    + " where u.USUA_RUT =  ? ";
            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setInt(1, numRut);
            rs = ps.executeQuery();
            if (rs.next()) {
                usuario.setId(rs.getInt("usua_id"));
                usuario.setLogin(rs.getString("USUA_LOGIN"));
                usuario.setNombre(rs.getString("usua_nombre").trim());
                usuario.setApellidoPaterno(rs.getString("usua_ap_paterno").trim());
                if (rs.getString("usua_ap_materno") != null) {
                    usuario.setApellidoMaterno(rs.getString("usua_ap_materno").trim());
                }
                String nombreFull = usuario.getNombre();
                if (usuario.getApellidoPaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoPaterno();
                }
                if (usuario.getApellidoMaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoMaterno();
                }
                usuario.setNombreFull(nombreFull);
                usuario.setIdEstadoUsuario(rs.getInt("USUA_ESUS_ID"));
                usuario.setEmail(rs.getString("USUA_EMAIL"));
            }
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "verificaUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
        return usuario;
    }

    /**
     *
     * @param login
     * @return
     * @throws BaseDatosException
     */
    public UsuarioVO obtieneUsuarioResetPassword(String login) throws BaseDatosException {
        UsuarioVO usuario = new UsuarioVO();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String select = "select u.[USUA_ID], USUA_LOGIN "
                    + ",u.[USUA_ESUS_ID] "
                    + ",u.[USUA_NOMBRE] "
                    + ",u.[USUA_AP_PATERNO] "
                    + ",u.[USUA_AP_MATERNO] "
                    + ",u.[USUA_EMAIL] "
                    + " FROM TB_USUARIO u "
                    + " where u.USUA_LOGIN =  ? ";
            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setString(1, login);
            rs = ps.executeQuery();
            if (rs.next()) {
                usuario.setId(rs.getInt("usua_id"));
                usuario.setLogin(rs.getString("USUA_LOGIN"));
                usuario.setNombre(rs.getString("usua_nombre").trim());
                usuario.setApellidoPaterno(rs.getString("usua_ap_paterno").trim());
                if (rs.getString("usua_ap_materno") != null) {
                    usuario.setApellidoMaterno(rs.getString("usua_ap_materno").trim());
                }
                String nombreFull = usuario.getNombre();
                if (usuario.getApellidoPaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoPaterno();
                }
                if (usuario.getApellidoMaterno() != null) {
                    nombreFull += Constantes.STR_ESPACIO + usuario.getApellidoMaterno();
                }
                usuario.setNombreFull(nombreFull);
                usuario.setIdEstadoUsuario(rs.getInt("USUA_ESUS_ID"));
                usuario.setEmail(rs.getString("USUA_EMAIL"));
            }
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, "verificaUsuario", sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
        return usuario;
    }

    /**
     *
     * @param usuario
     * @param newPass
     * @throws BaseDatosException
     */
    public void ingresarHistoricoPassword(UsuarioVO usuario, String newPass) throws BaseDatosException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            String updateSQL = "insert into TB_HISTORIA_PASS_USUARIO ("
                    + "HIPU_ID "
                    + ", HIPU_USUA_ID "
                    + ", HIPU_PASSWORD "
                    + ", HIPU_CREACION "
                    + ", HIPU_CREADOR "
                    + ") values(?,?,?,getdate(),?) ";
            //1 2 3           4
            long id = this.obtenerNextval("TB_HISTORIA_PASS_USUARIO");
            con = this.getConnection();
            ps = con.prepareStatement(updateSQL);
            ps.setLong(1, id);
            ps.setLong(2, usuario.getIdUsuario());
            ps.setString(3, newPass);
            ps.setLong(4, usuario.getIdUsuario());
            ps.executeUpdate();
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
            usuario.setIdAcceso(id);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, null);
            super.cierraConeccion(con);
        }

    }

    /**
     *
     * @param usuario
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    public void listaOpcionMenu(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String select = "SELECT [OMUS_OPME_ID], OPME_DESCRIPCION "
                    + "  FROM [dbo].[TB_OPCION_MENU_USUARIO] o, [dbo].[TB_USUARIO] u, [dbo].[TB_OPCION_MENU] om "
                    + "  where u.USUA_ID = o.OMUS_USUA_ID "
                    + "  and o.OMUS_OPME_ID = om.OPME_ID"
                    + "  and u.USUA_ID = ? "
                    + "  and o.OMUS_ACTIVO = ? ";
            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setLong(1, usuario.getIdUsuario());
            ps.setString(2, Constantes.SI);
            rs = ps.executeQuery();
            ArrayList<CodigoDescripcionVO> lista = new ArrayList<>();
            while (rs.next()) {
                CodigoDescripcionVO menu = new CodigoDescripcionVO();
                menu.setId(rs.getInt("OMUS_OPME_ID"));
                menu.setDescripcion(rs.getString("OPME_DESCRIPCION"));
                switch (menu.getId()) {
                    case Constantes.OPC_MENU_CARGA_ARCHIVOS:
                        usuario.setMenuCarga(true);
                        break;
                    case Constantes.OPC_MENU_CONSULTA_CARGA_ARCHIVOS:
                        usuario.setMenuConsultaCarga(true);
                        break;
                    case Constantes.OPC_MENU_CONVERSION_INTRADIA:
                        usuario.setMenuCargaConversion(true);
                        break;
                    case Constantes.OPC_MENU_CONSULTA_CONVERSION_INTRADIA:
                        usuario.setMenuConsultaCargaConversion(true);
                        break;
                    case Constantes.OPC_MENU_CONVERSION_TEFMASIVA:
                        usuario.setMenuCargaConversionTef(true);
                        break;
                    case Constantes.OPC_MENU_CONSULTA_CONVERSION_TEFMASIVA:
                        usuario.setMenuConsultaCargaConversionTef(true);
                        break;
                    default:
                        break;
                }

                lista.add(menu);
            }
            usuario.setListaOpcionesMenu(lista);
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        } catch (SQLException sqle) {
            Logger.getLogger(LoginDAO.class.getName()).log(Level.SEVERE, null, sqle);
            throw new BaseDatosException(String.valueOf(sqle.getErrorCode()));
        } finally {
            super.cierraCursores(ps, rs);
            super.cierraConeccion(con);
        }
    }
}
