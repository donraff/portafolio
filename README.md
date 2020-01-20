# portafolio
Este portafolio posee código Java relacionados con Firma digital, login y estandar XHTML.


# firma_archivo_txt (data de 2019)
contiene ejemplo de firma de archivo de texto plano.
Se crea par de llaves RSA con SHA1PRNG, Luego se firma con SHA256withRSA.
Escribe el archivo firmado en disco, escribe llave pública en disco.

También posee "validador" de firma, utilizando para ello la llave pública que se generó en el paso anterior.

# firma_digital XML (data de 2012)
Posee dos clases:
- EtokenSignerApplet: recupera datos desde invocación de servlet java, accesa a dispositivo eToken, valida fecha y revocación de certificado, aplica firma envolvente.
- VisadoSignerApplet: a diferencia del anterior, obtiene datos de sesión y agrega nodo al XML con la visación del documento.

# login
Ejemplo de Login con Struts, EJB, DAO.

# loginRest
Ejemplo sencillo de WS Rest para Login

# xhtml
Ejemplo de correcta implementación de estándar XHTML 1.1 Strict, valido por W3C.
