from support import *
anonymous=client()
for attempt in range(20):
 try:
  code,_,_=call(anonymous,'api/login/me');break
 except OSError:time.sleep(1)
expect(code,401,'sesión anónima')
expect(call(anonymous,'api/conexion/test')[0],401,'diagnóstico protegido')
expect(call(anonymous,'api/login','POST',{'nombreUsuario':'admin','contrasena':'incorrecta'})[0],401,'contraseña incorrecta')
sessions={}
for role in ['admin','gestor','proveedor','auditor']:
 opener=client()
 code,user,headers=call(opener,'api/login','POST',{'nombreUsuario':role,'contrasena':credentials[role]})
 expect(code,200,'login '+role)
 assert 'csrf' in user and 'permisos' in user and 'contrasenaHash' not in user
 assert 'HttpOnly' in headers.get('Set-Cookie','')
 sessions[role]=(opener,user['csrf'],user)
 expect(call(opener,'api/login/me')[0],200,'sesión '+role)
admin,token,_=sessions['admin']
expect(call(admin,'api/proveedores/999999/articulos','POST',{'idArticulo':999999,'precio':10},token)[0],409,'FK heredada devuelve conflicto uniforme')
code,health,_=call(admin,'api/conexion/test')
expect(code,200,'diagnóstico autenticado')
assert health['sqlServer'] and health['postgres'],'Pools reales'
expect(call(admin,'api/articulos','POST',{})[0],403,'mutación sin CSRF')
auditor,audit_token,_=sessions['auditor']
expect(call(auditor,'api/articulos')[0],200,'auditor lectura')
expect(call(auditor,'api/articulos','POST',{},audit_token)[0],403,'auditor escritura denegada')
gestor,gestor_token,_=sessions['gestor']
expect(call(gestor,'api/articulos','POST',{},gestor_token)[0],403,'gestor cambio catálogo denegado')
provider,provider_token,user=sessions['proveedor']
expect(call(provider,'api/proveedores/'+str(user['idProveedor'])+'/articulos')[0],200,'proveedor catálogo propio')
expect(call(provider,'api/proveedores/'+str(user['idProveedor']+1)+'/articulos')[0],403,'proveedor catálogo ajeno denegado')
item={'codigoArticulo':'TEST-SECURITY','nombre':'Prueba reversible','descripcion':'Caso automatizado temporal'}
code,created,_=call(admin,'api/articulos','POST',item,token)
expect(code,201,'CRUD crear artículo')
item_id=created['idArticulo']
try:
 item['nombre']='Prueba actualizada'
 expect(call(admin,'api/articulos/'+str(item_id),'PUT',item,token)[0],200,'CRUD actualizar artículo')
 expect(call(admin,'api/articulos/'+str(item_id))[0],200,'CRUD consultar artículo')
finally:
 expect(call(admin,'api/articulos/'+str(item_id),'DELETE',csrf=token)[0],200,'CRUD borrar artículo temporal')
expect(call(admin,'api/login/logout','POST',{},token)[0],200,'logout')
expect(call(admin,'api/articulos')[0],401,'sesión cerrada no reutilizable')
print('PASS pruebas HTTP de roles, catálogo propio, CSRF, pools y CRUD reversible')
