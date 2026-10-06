from support import *
from concurrent.futures import ThreadPoolExecutor
import datetime
admin=client();code,user,_=call(admin,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']});expect(code,200,'login límites');token=user['csrf']
for endpoint in ['articulos','sucursales','departamentos','proveedores']:
 expect(call(admin,'api/'+endpoint+'/1','PUT',None,token)[0],400,'cuerpo nulo '+endpoint)
expect(call(admin,'api/proveedores/1/articulos','POST',None,token)[0],400,'asociación nula')
code,records,_=call(admin,'api/gestion/pedidos?pageSize=100');assert code==200
pedido=next(p for p in records['items'] if p['id_orden'] is None)
code,types,_=call(admin,'api/gestion/tiposorden');assert code==200
typeid=types['items'][0]['id_tipo']
code,subtypes,_=call(admin,'api/gestion/subtiposorden');assert code==200
subtype=next(s['id_subtipo'] for s in subtypes['items'] if s['id_tipo']==typeid)
today=datetime.date.today()
body={'descripcion':'TEST-CONCURRENCIA','fecha_creacion':str(today),'fecha_limite_oferta':str(today+datetime.timedelta(days=3)),'id_tipo':typeid,'id_subtipo':subtype,'pedidoIds':[pedido['id_pedido']]}
workers=[]
for i in range(2):
 op=client();code,u,_=call(op,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']});assert code==200;workers.append((op,u['csrf']))
def submit(pair):return call(pair[0],'api/ordenes','POST',body,pair[1])
with ThreadPoolExecutor(max_workers=2) as pool:results=list(pool.map(submit,workers))
winners=[r for r in results if r[0]==201]
try:
 assert len(winners)==1,[(r[0],r[1]) for r in results]
 assert all(r[0] in [201,400,409] for r in results)
 print('PASS concurrencia: un solo encabezado y asignación; respuestas', [r[0] for r in results])
finally:
 for _,order,_ in winners:expect(call(admin,'api/ordenes/'+str(order['id_orden']),'DELETE',csrf=token)[0],204,'limpieza orden concurrente')
code,after,_=call(admin,'api/gestion/pedidos/'+str(pedido['id_pedido']));assert code==200 and after['id_orden'] is None
expect(call(admin,'api/gestion/articulos?q=%27%3BDELETE%20FROM%20Articulo%3B--')[0],200,'entrada SQL parametrizada')
print('PASS entradas nulas, concurrencia y limpieza transaccional')

viewers=[]
for i in range(6):
 op=client();code,_,_=call(op,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']});assert code==200;viewers.append(op)
with ThreadPoolExecutor(max_workers=6) as pool:dashboards=list(pool.map(lambda op:call(op,'api/dashboard'),viewers))
assert all(code==200 and data['pedidos']==100 for code,data,_ in dashboards)
print('PASS seis dashboards concurrentes sin retener dos conexiones del pool')
