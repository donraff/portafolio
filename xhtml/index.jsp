<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %> 
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<html:html xhtml="true">
    <head>
        <meta http-equiv="Pragma" content="no-cache" />
        <meta http-equiv="Cache-Control" content="no-cache" />
        <meta http-equiv="Expires" content="Sat, 01 Dec 2015 00:00:00 GMT" />
        <meta http-equiv="Content-Language" content="es-cl" />
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252" />
        <title><bean:message key="titulo.aplicacion"/></title>
        <link href="Styles/Style2016_int.css" rel="stylesheet" type="text/css" />		
        <script src="js/scripts.js" type="text/javascript"></script>
        <script type="text/javascript" charset="utf-8">
            function enviarForm2(accion) {
                document.forms[0].accion.value = accion;
                if (confirm('Se procederá a Activar/Desactivar la funcionalidad de Transferencias.\n ¿Está seguro?')) {
                    document.forms[0].submit();
                }
            }
            function enviarForm3(accion) {
                document.forms[0].accion.value = accion;
                if (confirm('Se procederá a Activar/Desactivar Transferencias vía Casilla STI.\n ¿Está seguro?')) {
                    document.forms[0].submit();
                }
            }

            function monitorIntegrado() {
                window.open("ccafast.do?accion=menu.monitor.integrado", "ppp", "width=700,height=800,menubar=no,status=no,toolbar=no");
            }
        </script>
    </head>
    <body onload="if (history.length > 0)
                history.go(+1);" >
        <html:form action="/ccafast">
            <div><html:hidden property="accion"/></div>
            <div><input type="hidden" name="OWASP_CSRFTOKEN" value="<csrf:token-value />"/></div>
            <table border="0" width="100%" cellpadding="0" cellspacing="0">
                <tr>
                    <td colspan="2">
                        <%@ include file="../header.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td class="fondo_menu01" style="width: 200px; vertical-align:top;">
                        <%@ include file="../datos_usuario.jsp" %>
                        <%@ include file="../menuComun.jsp" %>	
                    </td>
                    <td class="fondo_menu02" style="vertical-align:top; padding:20px; height:650px">
                        <table border="0">
                            <tr>
                                <td style="vertical-align:top">
                                    <p class="encabezadoHome">Funcionalidades del Sistema:</p>
                                    <table border="1" width="550px"  class="tablaGris">
                                        <thead>
                                            <tr>
                                                <th colspan="2">Opci&oacute;n de Men&uacute;</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:abrirPopupMonitor('menu.monitor');"><img src="Images/icon_monitor_32.png" alt="" style="vertical-align: middle;" /></a></td>
                                                <td><span class="tituloHome">Monitor Transferencias:</span>&nbsp;<span class="textoHome">Permite visualizar las transferencias del sistema desde y hacia Casillas.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:abrirPopupMonitor('menu.monitor.masivas');"><img src="Images/icon_monitor_32.png" alt="" style="vertical-align: middle;" /></a></td>
                                                <td><span class="tituloHome">Monitor TEF/TGR Masivas:</span>&nbsp;<span class="textoHome">Permite visualizar los movimientos de archivos TEF y TGR Masivas, inyectados al Tandem durante el d&iacute;a.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:abrirPopupMonitor('menu.monitor.ccacopiar');"><img src="Images/icon_monitor_32.png" alt="" style="vertical-align: middle;" /></a></td>
                                                <td><span class="tituloHome">Monitor CCA Copiar:</span>&nbsp;<span class="textoHome">Permite visualizar los movimientos de archivos Inyectados a StarACH por medio de CCA Copiar.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:abrirPopupMonitor('menu.monitor.integrado');"><img src="Images/icon_monitor_32.png" alt="" style="vertical-align: middle;" /></a></td>
                                                <td><span class="tituloHome">Monitor Integrado:</span>&nbsp;<span class="textoHome">Permite visualizar informaci&oacute;n integrada de funcionalidades CCA Fast (Transferencias, CCA Copiar, Tef/Tgr Masivas, Casillas CCA, procesamiento MicroTLF de TGR).</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:abrirPopupMonitor('menu.monitor.casillacca');"><img src="Images/icon_monitor_32.png" alt="" style="vertical-align: middle;" /></a></td>
                                                <td><span class="tituloHome">Monitor Casilla CCA:</span>&nbsp;<span class="textoHome">Permite visualizar informaci&oacute;n de transferencia de archivos procesados por Casilla CCA.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta Transferencias:</span>&nbsp;<span class="textoHome">Permite consultar archivos enviados/recibidos por el Sistema.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta.ccacopiar');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta CCA Copiar:</span>&nbsp;<span class="textoHome">Permite consultar archivos inyectados al StarACH provenientes del Sistema.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.alertas');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta Alertas:</span>&nbsp;<span class="textoHome">Permite consultar alertas emitidas por el Sistema en la transferencia de archivos (errores de comunicaciones y/o inconsistencias en el proceso).</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta.masivas');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta TEF/TGR Masivas:</span>&nbsp;<span class="textoHome">Permite consultar archivos enviados al Tandem, de los procesos de TEF y TGR Masivas.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta.contingencias');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta Contingencias StarACH:</span>&nbsp;<span class="textoHome">Permite consultar los bancos que han activado contingencia transaccional (de la tarde) de Batch Cr&eacute;dito y D&eacute;bito.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta.ctg.casilla');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta Contingencias Casilla STI:</span>&nbsp;<span class="textoHome">Permite consultar lo historia de las contingencias de Casilla STI registradas por el sistema.</span></td>
                                            </tr>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.consulta.contingencias');"><img src="Images/icon_search.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Consulta Casilla CCA:</span>&nbsp;<span class="textoHome">Permite consultar loa archivos transmitidos por Casilla CCA.</span></td>
                                            </tr>
                                            <logic:equal name="usuarioVO" scope="session" property="idPerfil" value="1">
                                                <tr class="row0">
                                                    <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.mantenedor.banco');"><img src="Images/ico_bank_blue64.png" height="32" width="32" alt="" style="vertical-align: middle;"  /></a></td>
                                                    <td><span class="tituloHome">Mantenedor Bancos:</span>&nbsp;<span class="textoHome">Permite modificar el Modo de Operaci&oacute;n de cada Banco para el funcionamiento con Casillas STI y CCA.</span></td>
                                                </tr>
                                                <tr class="row0">
                                                    <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.mantenedor.tipo');"><img src="Images/ico-edit_32.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                    <td><span class="tituloHome">Mantenedor Tipo de Archivo:</span>&nbsp;<span class="textoHome">Permite activar/desactivar el env&iacute;o / recepci&oacute;n por Tipo de Archivo.</span></td>
                                                </tr>
                                            </logic:equal>
                                            <tr class="row0">
                                                <td style="width: 40px;"><a href="#" onclick="javascript:enviarForm('menu.mantenedor.contingencia');"><img src="Images/icon-doc-time_32.png" alt="" style="vertical-align: middle;"  /></a></td>
                                                <td><span class="tituloHome">Mantenedor Contingencias StarACH:</span>&nbsp;<span class="textoHome">Permite indicar Archivos que ser&aacute;n procesados por CCA Copiar en Ciclo de Contingencia.</span></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td style="width:40px">&nbsp;</td>
                                <td  style="vertical-align:top">
                                    <p class="encabezadoHome">Activar / Desactivar Transferencias:</p>
                                    <table border="1" width="500px" class="tabla-home">
                                        <tr>
                                            <td style="width: 40px; vertical-align:top"><logic:equal name="CCAFast" property="activo" value="S"><img src="Images/bullet-green.png" height="32" width="32" alt="" style="vertical-align: middle;"  /></logic:equal>
                                                <logic:equal name="CCAFast" property="activo" value="N"><img src="Images/bullet-red.png" height="32" width="32" alt="" style="vertical-align: middle;"  /></logic:equal>
                                                </td>
                                                <td><span class="tituloHome">Estado Actual: <logic:equal name="CCAFast" property="activo" value="S">Activo</logic:equal><logic:equal name="CCAFast" property="activo" value="N">Desactivado</logic:equal></span><br/><br/>
                                                    <span class="textoHome"><strong>Activo:</strong> permite la ejecuci&oacute;n de los procesos de Env&iacute;o / Recepci&oacute;n de archivos.<br/>
                                                        <strong>Desactivado:</strong> inhabilita las procesos, es decir, no se enviar&aacute;n ni recibir&aacute;n archivos por CCA Fast.<br/><br/>
                                                        <a href="#" onclick="javascript:enviarForm2('activa.desactiva');">
                                                        <logic:equal name="CCAFast" property="activo" value="S">Desactivar Transferencias</logic:equal><logic:equal name="CCAFast" property="activo" value="N">Activar Transferencias</logic:equal>
                                                        </a></span></td>
                                            </tr>
                                        </table>
                                    <logic:equal name="usuarioVO" scope="session" property="idPerfil" value="1">
                                        <p class="encabezadoHome">Activar / Desactivar Contingencia Casilla:</p>
                                        <bean:define name="CCAFast" property="ultCTG" id="ultCTG" />
                                        <table border="1" width="500px" class="tabla-home">
                                            <tr>
                                                <td style="width: 40px; vertical-align:top"><logic:equal name="CCAFast" property="ctgActiva" value="S"><img src="Images/bullet-red.png" height="32" width="32" alt="" style="vertical-align: middle;"  /></logic:equal>
                                                    <logic:equal name="CCAFast" property="ctgActiva" value="N"><img src="Images/bullet-green.png" height="32" width="32" alt="" style="vertical-align: middle;"  /></logic:equal></td>
                                                <td><span class="tituloHome">Estado Actual: <logic:equal name="CCAFast" property="ctgActiva" value="S">Activo. Fecha Inicio: <bean:write name="ultCTG" property="fechaInicio"  format="dd/MM/yyyy HH:mm:ss"/></logic:equal>
                                                        <logic:equal name="CCAFast" property="ctgActiva" value="N">Desactivado</logic:equal><br/>
                                                        </span><br/>
                                                        <span class="textoHome"><strong>Activo:</strong> Env&iacute;o y Recuperaci&oacute;n de archivos solo por Casilla CCA.<br/>
                                                            <strong>Desactivado:</strong> Estado "normal" de proceso, los archivos se transmitir&aacute;n por ambas casillas segun corresponda.<br/><br/>
                                                        <logic:equal name="CCAFast" property="ctgActiva" value="S"><a href="#" onclick="javascript:enviarForm3('ctg.casilla.desactivar');">Desactivar Contingencia Casilla</a></logic:equal>
                                                        <logic:equal name="CCAFast" property="ctgActiva" value="N"><a href="#" onclick="javascript:enviarForm3('ctg.casilla.activar');">Activar Contingencia Casilla</a></logic:equal>
                                                        </span>
                                                    </td>
                                                </tr>
                                            </table>
                                    </logic:equal>
                                </td>
                            </tr>
                        </table>
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <%@ include file="../pie.jsp" %>
                    </td>
                </tr>
            </table>
        </html:form>
    </body>
</html:html>