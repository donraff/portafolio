<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %> 
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<fmt:setLocale value = "es_CL"/>
<%@ page buffer = "64kb" %>
<html:html xhtml="true">
    <head>
        <meta http-equiv="Content-Language" content="es-cl" />
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252" />
        <title><bean:message key="titulo.aplicacion"/></title>
        <link href="Styles/Style2016_int.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="Styles/dhtmlxcalendar.css" />
        <link rel="stylesheet" type="text/css" href="Styles/skins/dhtmlxcalendar_omega.css" />
        <script src="js/dhtmlxcalendar.js" type="text/javascript" ></script>        
        <script src="js/jquery-3.4.1.min.js" type="text/javascript" ></script>
        <script src="js/scripts.js" type="text/javascript"></script>
        <script type="text/javascript">
            var myCalendar;
            function calendario() {
                myCalendar = new dhtmlXCalendarObject(["calendar", "calendar2"]);
                myCalendar.setSkin('omega');
                myCalendar.hideTime();
                var curDateObj = myCalendar.getDate();
                myCalendar.setSensitiveRange("01/07/2017", curDateObj);
            }
            function enviarForm2(accion) {
                document.forms[0].accion.value = accion;
                document.forms[0].submit();
            }
            function popup(idArr) {
                var f = document.getElementById('TheForm2');
                f.target = "TheWindow2";
                f.idArr.value = idArr;
                f.accion.value = "consulta.casillacca.detalle";
                window.open('', 'TheWindow2', "width=580,height=680,scrollbars=no,location=no,top=200,left=600,resizable=no");
                f.submit();               
            }
        </script>

    </head>
    <body onload="calendario();
            if (history.length > 0)
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
                        
                        <p class="encabezadoHome">Consulta Casilla CCA</p>
                        <table border="0" width="1100" class="tabla-busqueda">
                            <tr>
                                <td colspan="3" class="titulo-funcion">Formulario de B&uacute;squeda</td>
                            </tr>
                            <tr class="titulos">
                                <td style="width: 250px; padding-left:10px;">Casilla Origen</td>
                                <td style="width: 250px; padding-left:10px;">Destino CTR</td>
                                <td style="width: 250px; padding-left:10px;">Estado Casilla</td>
                            </tr>
                            <tr>
                                <td style="padding-left:10px;"><html:select property="origen"><html:options collection="LISTA_BANCOS_CASILLA" labelProperty="descripcion" property="idSTR"/></html:select></td>
                                <td style="padding-left:10px;"><html:select property="destino"><html:options collection="LISTA_BANCOS_CASILLA" labelProperty="descripcion" property="idSTR"/></html:select></td>
                                <td style="padding-left:10px;"><html:select property="estado"><html:options collection="LISTA_ESTADOS_CASILLA" labelProperty="descripcion" property="id"/></html:select></td>
                            </tr>
                            <tr class="titulos">
                                <td style="width: 250px; padding-left:10px;">Nombre Archivo</td>
                                <td style="width: 250px; padding-left:10px;">Fecha Creaci&oacute;n (inicio)</td>
                                <td style="width: 250px; padding-left:10px;">Fecha Creaci&oacute;n (fin)</td>
                            </tr>
                            <tr>
                                <td style="padding-left:10px;"><html:text property="nombre" maxlength="20" style="width:180px; " /></td>
                                <td style="padding-left:10px;"><html:text property="fechaInicioProceso" size="15" maxlength="10" styleId="calendar" readonly="true" /></td>
                                <td style="padding-left:10px;"><html:text property="fechaFinProceso" size="15" maxlength="10" styleId="calendar2" readonly="true" /></td>
                            </tr>  
                        </table>
                        <br/>
                        <table border="0">
                            <tr>
                                <td style="width: 75px; padding-left:10px;"><a class="btn btn-primary btn-sm" href="#" onclick="javascript:enviarForm('consulta.casillacca');" id="btnSearch"><span class="glyphicon glyphicon-search"></span><span class=""> Buscar</span></a></td>
                                <td style="width: 75px; padding-left:10px;"><a class="btn btn-default btn-xs" href="#" onclick="javascript:enviarForm('menu.consulta.casillacca');" id="btnClear"><span class=" icon-undo"></span><span class="">Limpiar</span></a></td>
                            </tr>
                        </table>
                        <br/>
                        <logic:present name="LISTA_CONSULTA_ARCH_CASILLA" scope="session">
                            <logic:notEmpty name="LISTA_CONSULTA_ARCH_CASILLA" scope="session">
                                <p class="encabezadoHome">Resultado B&uacute;squeda:</p>
                                <display:table export="true"  id="LISTA_CONSULTA_ARCH_CASILLA"
                                               name="sessionScope.LISTA_CONSULTA_ARCH_CASILLA"
                                               requestURI="/ccafast.do?accion=consulta.casillacca.pag"
                                               pagesize="20" class="tablaGris" excludedParams="*"
                                               style="width: 99%">
                                    <display:setProperty name="export.excel.filename" value="ListaArchivosCasillaCCA.xls"/>
                                    <display:column title="ID" sortProperty="id" sortable="true" style="text-align:right;">
                                        <fmt:formatNumber type = "number" pattern = "#,##0" value = "${LISTA_CONSULTA_ARCH_CASILLA.id}" />
                                    </display:column>
                                    <display:column property="casillaOrigen" title="Casilla Origen" sortable="true"/>
                                    <display:column property="origen" title="Origen CTR" sortable="true"/>
                                    <display:column property="destino" title="Destino CTR" sortable="true"/>
                                    <display:column property="nombre" title="Nombre Archivo" sortable="true"/>
                                    <display:column property="tamanoDatosSTR" title="Tamaño Archivo" sortable="true" sortProperty="tamanoDatos" style="text-align:right;"/>
                                    <display:column title="Cant Regs" sortProperty="cantRegsDatos" sortable="true" style="text-align:right;">
                                        <fmt:formatNumber type = "number" pattern = "#,##0" value = "${LISTA_CONSULTA_ARCH_CASILLA.cantRegsDatos}" />
                                    </display:column>
                                    <display:column property="largoLinDatos" title="Largo Lin" sortable="true" format="{0,number,#,###}" style="text-align:right;"/>
                                    <display:column property="fv" title="CTR FV" sortable="true"/>
                                    <display:column property="formatoOrig" title="CTR Formato Orig" sortable="true"/>
                                    <display:column property="traducc" title="CTR Traducc" sortable="true"/>
                                    <display:column property="fechaCreacion" title="Fecha Creación" sortable="true" format="{0,date,dd/MM/yyyy HH:mm:ss}"/>
                                    <display:column property="fechaRes" title="Fecha RES" sortable="true" format="{0,date,dd/MM/yyyy HH:mm:ss}"/>
                                    <display:column property="fechaDestino" title="Fecha Destino" sortable="true" format="{0,date,dd/MM/yyyy HH:mm:ss}"/>
                                    <display:column property="fechaModificacion" title="Fecha Modificacion" sortable="true" format="{0,date,dd/MM/yyyy HH:mm:ss}"/>
                                    <display:column property="estado" title="Estado" sortable="true" />
                                    <display:column property="mensajeRES" title="Mensaje RES" sortable="true" />
                                    <display:column title="Detalle" sortable="false" media="html">
                                        <a href="#" onclick="popup(<c:out value="${LISTA_CONSULTA_ARCH_CASILLA_rowNum -1}"/>);"><img src="Images/icon_search.png" style="vertical-align: middle;" width="20" height="20" alt="" title=""/></a>
                                    </display:column>
                                </display:table>                                
                            </logic:notEmpty> 
                            <logic:empty name="LISTA_CONSULTA_ARCH_CASILLA" scope="session">
                                <span class="pagebanner">Resultado de la b&uacute;squeda: No se han encontrado registros.</span>
                            </logic:empty>
                        </logic:present>                                                
                        
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
        <form id="TheForm2" method="post" action="ccafast.do" >
            <div><input type="hidden" name="accion" /></div>
            <div><input type="hidden" name="idArr" /></div>
        </form>             
    </body>
</html:html>