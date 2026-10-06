from support import client,call,expect,credentials
admin=client();code,user,_=call(admin,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']})
expect(code,200,'login informes')
code,dash,_=call(admin,'api/dashboard');expect(code,200,'dashboard real')
assert dash['articulos']==50 and dash['proveedores']==20 and dash['pedidos']==100 and dash['adjudicaciones']==10
assert dash['montoAdjudicado']>0 and dash['ordenesAbiertas']==10
code,orders,_=call(admin,'api/ordenes?pageSize=100');expect(code,200,'órdenes para comparación')
awarded=next(o['id_orden'] for o in orders['items'] if o['estado']=='Adjudicada')
code,catalog,_=call(admin,'api/reportes');expect(code,200,'catálogo ocho informes');assert len(catalog['informes'])==8
for report in range(1,9):
 query='?orden='+str(awarded) if report==3 else ''
 code,data,_=call(admin,'api/reportes/'+str(report)+query);expect(code,200,'informe académico '+str(report))
 assert data['items'],'Informe sin datos '+str(report)
 if report==2:assert len(data['items'])<=5
 if report==3:assert sum(1 for row in data['items'] if row['ganadora'])==3
 if report==4:assert len(data['items'])==40
 if report==5:assert len(data['items'])==10
 if report==7:assert data['items'][0]['promedio_dias']==12
expect(call(admin,'api/reportes/1?desde=2026-12-31&hasta=2026-01-01')[0],400,'rango invertido')
expect(call(admin,'api/reportes/3')[0],400,'comparación requiere orden')
provider=client();code,_,_=call(provider,'api/login','POST',{'nombreUsuario':'proveedor','contrasena':credentials['proveedor']})
expect(code,200,'login proveedor informes')
expect(call(provider,'api/reportes/6')[0],403,'gasto institucional protegido')
code,data,_=call(provider,'api/reportes/8');expect(code,200,'precios propios')
assert len(set(row['proveedor'] for row in data['items']))==1
code,audit,_=call(admin,'api/auditoria');expect(code,200,'auditoría persistida');assert audit['total']>0
print('PASS ocho consultas, filtros, montos, ganadoras, tiempos, dashboard real y alcance por proveedor')
