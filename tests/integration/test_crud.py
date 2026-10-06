from support import client,call,expect,credentials
import urllib.parse,datetime
admin=client()
code,user,_=call(admin,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']})
expect(code,200,'login CRUD');token=user['csrf']
def list_items(module):
 code,data,_=call(admin,'api/gestion/'+module+'?pageSize=100');expect(code,200,'listar '+module)
 assert data['total']>=len(data['items'])
 return data['items']
def path(module,key):return 'api/gestion/'+module+'/'+urllib.parse.quote(str(key),safe='~')
def create(module,data):
 code,record,_=call(admin,'api/gestion/'+module,'POST',data,token);expect(code,201,'crear '+module)
 assert 'contrasena_hash' not in record
 return record
def update(module,record,data):
 expect(call(admin,path(module,record['_key']),'PUT',data,token)[0],200,'actualizar '+module)
def delete(module,record):expect(call(admin,path(module,record['_key']),'DELETE',csrf=token)[0],204,'borrar '+module)
role=create('roles',{'nombre_rol':'TEST-ROL','descripcion':'Prueba reversible'})
screen=create('pantallas',{'nombre_pantalla':'TEST-PANTALLA','descripcion':'Prueba'})
permission_data={'id_rol':role['id_rol'],'id_pantalla':screen['id_pantalla'],'permite_crear':False,'permite_leer':True,'permite_actualizar':False,'permite_borrar':False}
permission=create('permisos',permission_data)
update('permisos',permission,{**permission_data,'permite_actualizar':True})
account_data={'nombre_usuario':'test_crud','contrasena':'TemporalSegura2026!','nombre_completo':'Cuenta temporal','email':'test_crud@demo.local','id_rol':role['id_rol'],'id_proveedor':None,'estado':'Activo'}
account=create('usuarios',account_data)
update('usuarios',account,{**account_data,'estado':'Inactivo','contrasena':''})
expect(call(admin,path('roles',role['_key']),'DELETE',csrf=token)[0],409,'borrado bloqueado por referencias')
delete('usuarios',account);delete('permisos',permission);delete('pantallas',screen);delete('roles',role)
branch_data={'codigo_sucursal':'TEST-SUC','direccion':'Dirección temporal','ciudad':'Salamá','region':'Baja Verapaz','telefono':'5555-1234'}
branch=create('sucursales',branch_data);update('sucursales',branch,{**branch_data,'direccion':'Dirección actualizada'})
phone=create('telefonos',{'id_sucursal':branch['id_sucursal'],'telefono':'5555-5678'})
delete('telefonos',phone)
dep_data={'id_sucursal':branch['id_sucursal'],'nombre':'TEST-DEP','descripcion':'Temporal'}
dep=create('departamentos',dep_data);update('departamentos',dep,{**dep_data,'descripcion':'Actualizado'})
article_data={'codigo_articulo':'TEST-CAT','nombre':'Artículo temporal','descripcion':'Temporal','estado':'Activo'}
article=create('articulos',article_data);update('articulos',article,{**article_data,'descripcion':'Actualizado'})
provider_data={'codigo_proveedor':'TEST-PRV','nombre_comercial':'Proveedor temporal','direccion':'Local temporal','telefono':'5555-1234','categoria':'Papelería','estado':'Activo'}
provider=create('proveedores',provider_data);update('proveedores',provider,{**provider_data,'direccion':'Local actualizado'})
provider2=create('proveedores',{**provider_data,'codigo_proveedor':'TEST-PRV2','nombre_comercial':'Proveedor segundo'})
catalog_data={'id_proveedor':provider['id_proveedor'],'id_articulo':article['id_articulo'],'precio':15.5}
catalog=create('proveedorarticulos',catalog_data);update('proveedorarticulos',catalog,{**catalog_data,'precio':20})
expect(call(admin,'api/gestion/proveedorarticulos','POST',{**catalog_data,'precio':-1},token)[0],400,'validación precio catálogo')
rubro=create('rubros',{'id_proveedor':provider['id_proveedor'],'rubro':'Tecnología'})
relation_data={'id_proveedor_a':provider['id_proveedor'],'id_proveedor_b':provider2['id_proveedor']}
relation=create('relaciones',relation_data);update('relaciones',relation,relation_data)
today=datetime.date.today()
pedido_data={'id_departamento':dep['id_departamento'],'id_articulo':article['id_articulo'],'cantidad':10,'fecha_solicitud':str(today),'fecha_necesaria':str(today+datetime.timedelta(days=3))}
pedido=create('pedidos',pedido_data);update('pedidos',pedido,{**pedido_data,'cantidad':12})
expect(call(admin,'api/gestion/pedidos','POST',{**pedido_data,'cantidad':0},token)[0],400,'cantidad positiva')
expect(call(admin,path('pedidos',pedido['_key']),'PUT',{**pedido_data,'fecha_necesaria':str(today-datetime.timedelta(days=1))},token)[0],400,'fecha necesaria posterior')
delete('pedidos',pedido);delete('relaciones',relation);delete('rubros',rubro);delete('proveedorarticulos',catalog)
delete('proveedores',provider2);delete('proveedores',provider);delete('articulos',article);delete('departamentos',dep);delete('sucursales',branch)
for module in ['tiposorden','subtiposorden','roles','pantallas','permisos','usuarios','articulos','proveedores','pedidos','ofertas']:
 list_items(module)
expect(call(admin,'api/gestion/articulos?q='+urllib.parse.quote("' OR 1=1;--"))[0],200,'búsqueda preparada')
expect(call(admin,'api/gestion/articulos/9999999')[0],404,'registro inexistente')
expect(call(admin,'api/gestion/usuarios','POST',{**account_data,'id_rol':9999999},token)[0],400,'rol inexistente')
print('PASS CRUD faltantes, compuestos, validaciones, referencias, paginación y búsqueda preparada')

for module,field in [("departamentos","codigo_sucursal"),("proveedorarticulos","proveedor"),("pedidos","articulo")]:
 code,rows,_=call(admin,"api/gestion/"+module);expect(code,200,"JOIN legible "+module);assert isinstance(rows["items"][0][field],str) and rows["items"][0][field]
