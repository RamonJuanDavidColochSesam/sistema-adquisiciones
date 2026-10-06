from support import client,call,expect,credentials
import datetime
admin=client();code,user,_=call(admin,'api/login','POST',{'nombreUsuario':'admin','contrasena':credentials['admin']})
expect(code,200,'login adquisiciones');token=user['csrf']
provider=client();code,provider_user,_=call(provider,'api/login','POST',{'nombreUsuario':'proveedor','contrasena':credentials['proveedor']})
expect(code,200,'login ofertante');provider_token=provider_user['csrf']
def get(path):
 code,data,_=call(admin,path);expect(code,200,'consulta '+path);return data
pedido_ids=[];offers=[];order_id=None;award_id=None;evaluation=None
try:
 dep=get('api/gestion/departamentos')['items'][0]['id_departamento']
 articles=get('api/gestion/articulos')['items'][:2]
 today=datetime.date.today()
 pedido_ids=[]
 for index,article in enumerate(articles):
  body={'id_departamento':dep,'id_articulo':article['id_articulo'],'cantidad':10+index*10,'fecha_solicitud':str(today),'fecha_necesaria':str(today+datetime.timedelta(days=15))}
  code,pedido,_=call(admin,'api/gestion/pedidos','POST',body,token);expect(code,201,'solicitud temporal')
  pedido_ids.append(pedido['id_pedido'])
 types=get('api/gestion/tiposorden')['items'];subtypes=get('api/gestion/subtiposorden')['items']
 type_id=types[0]['id_tipo'];subtype=next(s['id_subtipo'] for s in subtypes if s['id_tipo']==type_id)
 order_body={'descripcion':'TEST-FLUJO-TRANSACCIONAL','observaciones':'Orden temporal de verificación','fecha_creacion':str(today),'fecha_limite_oferta':str(today+datetime.timedelta(days=5)),'id_tipo':type_id,'id_subtipo':subtype,'pedidoIds':pedido_ids}
 code,planned,_=call(admin,'api/ordenes','POST',{**order_body,'fecha_creacion':str(today+datetime.timedelta(days=1))},token);expect(code,201,'orden programada')
 order_id=planned['id_orden'];assert planned['estado']=='Programada'
 assert all(o['id_orden']!=order_id for o in get('api/reportes/5')['items']),'VIEW no abre período antes de creación'
 expect(call(admin,'api/ordenes/'+str(order_id),'DELETE',csrf=token)[0],204,'borrar orden programada temporal')
 order_id=None
 expect(call(admin,'api/ordenes','POST',{**order_body,'fecha_creacion':str(today-datetime.timedelta(days=1))},token)[0],400,'orden anterior a solicitud')
 assert all(get('api/gestion/pedidos/'+str(p))['id_orden'] is None for p in pedido_ids),'Rollback asignación'
 code,order,_=call(admin,'api/ordenes','POST',order_body,token);expect(code,201,'crear orden agrupada')
 order_id=order['id_orden'];assert len(order['pedidos'])==2 and order['observaciones']=='Orden temporal de verificación'
 expect(call(admin,'api/ordenes','POST',order_body,token)[0],400,'impedir pedido en segunda orden')
 order_body['descripcion']='TEST-FLUJO-ACTUALIZADO'
 expect(call(admin,'api/ordenes/'+str(order_id),'PUT',order_body,token)[0],200,'actualizar orden sin ofertas')
 offers=[]
 for index,pedido in enumerate(pedido_ids):
  offer_body={'id_pedido':pedido,'precio_unitario':10+index*20,'fecha_oferta':str(today),'observaciones':'Oferta temporal verificada'}
  code,offer,_=call(provider,'api/gestion/ofertas','POST',offer_body,provider_token);expect(code,201,'oferta de proveedor propio')
  offers.append(offer)
 second_supplier=next(p['id_proveedor'] for p in get('api/gestion/proveedores')['items'] if p['id_proveedor']!=provider_user['idProveedor'])
 code,tied,_=call(admin,'api/gestion/ofertas','POST',{'id_proveedor':second_supplier,'id_pedido':pedido_ids[0],'precio_unitario':10,'fecha_oferta':str(today),'observaciones':'Prueba de empate'},token);expect(code,201,'oferta con empate')
 offers.append(tied)
 assert sum(f['empate'] for f in get('api/ordenes/'+str(order_id))['ofertas'])==2
 expect(call(admin,'api/ordenes/'+str(order_id),'DELETE',csrf=token)[0],400,'preservar orden con ofertas')
 details=[{'id_pedido':p,'id_oferta':o['id_oferta'],'cantidad_final':5+i*3,'precio_acordado':10+i*20} for i,(p,o) in enumerate(zip(pedido_ids,offers))]
 assert all('diferencia_precio' in f and 'mejor_precio' in f for f in get('api/ordenes/'+str(order_id))['ofertas'])
 award_body={'id_orden':order_id,'fecha_resolucion':str(today),'observaciones':'Resolución verificada','detalles':details}
 before=len(get('api/adjudicaciones')['items'])
 expect(call(admin,'api/adjudicaciones','POST',{**award_body,'detalles':[{**details[0],'precio_acordado':11},details[1]]},token)[0],400,'precio debe corresponder a oferta')
 expect(call(admin,'api/adjudicaciones','POST',{**award_body,'detalles':details[:1]},token)[0],400,'adjudicación debe incluir todos los pedidos')
 bad=[{**details[0],'id_oferta':offers[1]['id_oferta']},details[1]]
 expect(call(admin,'api/adjudicaciones','POST',{**award_body,'detalles':bad},token)[0],400,'oferta no corresponde al pedido')
 assert len(get('api/adjudicaciones')['items'])==before,'Rollback adjudicación incompleta'
 code,award,_=call(admin,'api/adjudicaciones','POST',award_body,token);expect(code,201,'adjudicación atómica')
 award_id=award['id_adjudicacion'];assert len(award['detalles'])==2 and award['observaciones']=='Resolución verificada'
 assert get('api/ordenes/'+str(order_id))['estado']=='Adjudicada'
 expect(call(admin,'api/adjudicaciones','POST',award_body,token)[0],400,'única adjudicación por orden')
 expect(call(provider,'api/gestion/ofertas/'+str(offers[0]['id_oferta']),'PUT',{'id_pedido':pedido_ids[0],'precio_unitario':9,'fecha_oferta':str(today),'observaciones':'Oferta temporal verificada'},provider_token)[0],400,'no alterar oferta adjudicada')
 evaluation_body={'id_detalle_adjudicacion':award['detalles'][0]['id_detalle_adjudicacion'],'calificacion':4,'comentario':'Prueba temporal de evaluación','fecha':str(today)}
 code,evaluation,_=call(admin,'api/gestion/evaluaciones','POST',evaluation_body,token);expect(code,201,'evaluación proveedor')
 expect(call(admin,'api/gestion/evaluaciones/'+str(evaluation['id_evaluacion']),'PUT',{**evaluation_body,'calificacion':5},token)[0],200,'actualizar evaluación')
 expect(call(admin,'api/adjudicaciones/'+str(award_id),'DELETE',csrf=token)[0],400,'conservar adjudicación evaluada')
 expect(call(admin,'api/gestion/evaluaciones/'+str(evaluation['id_evaluacion']),'DELETE',csrf=token)[0],204,'borrar evaluación temporal')
 expect(call(admin,'api/adjudicaciones/'+str(award_id),'PUT',award_body,token)[0],200,'actualizar adjudicación completa')
 expect(call(admin,'api/adjudicaciones/'+str(award_id),'DELETE',csrf=token)[0],204,'revertir adjudicación temporal')
 for offer in offers:expect(call(admin,'api/gestion/ofertas/'+str(offer['id_oferta']),'DELETE',csrf=token)[0],204,'borrar oferta temporal')
 expect(call(admin,'api/ordenes/'+str(order_id),'DELETE',csrf=token)[0],204,'borrar orden y liberar pedidos')
 for pedido in pedido_ids:
  assert get('api/gestion/pedidos/'+str(pedido))['id_orden'] is None
  expect(call(admin,'api/gestion/pedidos/'+str(pedido),'DELETE',csrf=token)[0],204,'borrar solicitud temporal')
 print('PASS flujo pedido-orden-oferta-adjudicación-evaluación, CRUD, integridad y rollback por API')
except Exception:
 # Limpiar únicamente IDs creados por esta ejecución; conservar el error original.
 for path in (["api/gestion/evaluaciones/"+str(evaluation['id_evaluacion'])] if evaluation else [])+(["api/adjudicaciones/"+str(award_id)] if award_id else [])+["api/gestion/ofertas/"+str(o['id_oferta']) for o in offers]+(["api/ordenes/"+str(order_id)] if order_id else [])+["api/gestion/pedidos/"+str(p) for p in pedido_ids]:
  try:
   status,_,_=call(admin,path,'DELETE',csrf=token)
   if status not in (200,204,404):print('CLEANUP REQUIERE REVISIÓN',path,status)
  except Exception as cleanup_error:print('CLEANUP ERROR',type(cleanup_error).__name__)
 raise
