'use strict';
const $ = id => document.getElementById(id);
const el = (tag, text, className) => { const n=document.createElement(tag);if(text!==undefined)n.textContent=text;if(className)n.className=className;return n; };
const money = value => new Intl.NumberFormat('es-GT',{style:'currency',currency:'GTQ'}).format(Number(value||0));
const dateOnly = (date=new Date()) => date.getFullYear()+'-'+String(date.getMonth()+1).padStart(2,'0')+'-'+String(date.getDate()).padStart(2,'0');
const MODULES=[
 ['dashboard','Resumen institucional','Dashboard','▦'],['pedidos','Solicitudes de compra','Pedidos','▤'],['ordenes','Órdenes de compra','Ordenes','◇'],['ofertas','Ofertas de proveedores','Ofertas','↗'],['adjudicaciones','Adjudicaciones','Adjudicaciones','✓'],
 ['articulos','Artículos','Articulos','□'],['proveedores','Proveedores','Proveedores','♧'],['proveedorarticulos','Catálogo de proveedores','ProveedorArticulos','≡'],['sucursales','Sucursales','Sucursales','⌂'],['departamentos','Departamentos','Departamentos','⊞'],
 ['telefonos','Teléfonos de sucursal','Sucursales','◦'],['rubros','Rubros de proveedores','Proveedores','◦'],['relaciones','Relaciones comerciales','Relaciones','⇄'],['tiposorden','Tipos de orden','TiposOrden','◦'],['subtiposorden','Subtipos de orden','TiposOrden','◦'],
 ['evaluaciones','Evaluación de proveedores','Evaluaciones','☆'],['reportes','Informes y consultas','Reportes','▥'],['usuarios','Usuarios','Usuarios','♙'],['roles','Roles','Roles','◦'],['pantallas','Pantallas','Pantallas','◦'],['permisos','Matriz de permisos','Permisos','◦'],['conexion','Conexión de bases','Conexion','↔'],['auditoria','Historial de cambios','Auditoria','◷']
];
let user, active='dashboard', page=1, query='', ticket=0;
const permission=module=>user.permisos.find(p=>p.pantalla===MODULES.find(m=>m[0]===module)?.[2])||{};
async function api(path,options={}) {
 const response=await fetch('api/'+path,options);
 if(response.status===401){location.href='login.html';throw Error('La sesión terminó');}
 if(response.status===204)return null;
 let data;try{data=await response.json();}catch{throw Error('El servidor devolvió una respuesta no válida');}
 if(!response.ok)throw Error(data.error||'No se pudo completar la operación');
 return data;
}
const send=(path,method,body)=>api(path,{method,headers:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});
function notify(message,success=false){$('notice').textContent=message;$('notice').className=success?'success':'';$('notice').hidden=false;}
function clearNotice(){$('notice').hidden=true;}
function action(label,handler,style='secondary small'){const b=el('button',label,style);b.type='button';b.addEventListener('click',()=>Promise.resolve(handler()).catch(e=>notify(e.message)));return b;}
function cellValue(value,key){
 if(value===null||value===undefined)return el('span','—');
 if(typeof value==='boolean')return el('span',value?'Sí':'No');
 if(key==='estado'){const s=String(value);return el('span',s,'badge'+(['Inactivo','Vencida'].includes(s)?' inactive':''));}
 if(key==='ganadora')return el('span',Number(value)===1?'Seleccionada':'—','badge');
 if(/precio|monto|gasto/.test(key))return el('span',money(value));
 return el('span',String(value));
}
function table(items,fields,actions) {
 const wrap=el('div',undefined,'table-wrap');
 if(!items.length){wrap.append(el('div','No hay registros para esta consulta.','empty'));return wrap;}
 const table=el('table'),head=el('thead'),hr=el('tr'),body=el('tbody');
 const keys=fields||Object.keys(items[0]).filter(k=>!['_key','contrasena_hash','detalles','pedidos','ofertas','adjudicacion'].includes(k));
 keys.forEach(k=>hr.append(el('th',typeof k==='object'?k.etiqueta:label(k))));
 if(actions)hr.append(el('th','Acciones'));head.append(hr);table.append(head,body);
 items.forEach(item=>{const row=el('tr');if(item.ganadora===1)row.className='winning-offer';else if(item.mejor_precio===true)row.className='best-offer';keys.forEach(field=>{const key=typeof field==='object'?field.nombre:field;const td=el('td');td.append(cellValue(item[key],key));row.append(td);});if(actions){const td=el('td'),box=el('div',undefined,'row-actions');actions(item).forEach(b=>box.append(b));td.append(box);row.append(td);}body.append(row);});
 wrap.append(table);return wrap;
}
function label(key){return ({id_orden:'Orden',id_pedido:'Pedido',id_oferta:'Oferta',id_adjudicacion:'Adjudicación',fecha_creacion:'Fecha creación',fecha_limite_oferta:'Límite de ofertas',fecha_resolucion:'Resolución',codigo_sucursal:'Sucursal',nombre_usuario:'Usuario',nombre_comercial:'Proveedor',precio_unitario:'Precio unitario',cantidad_final:'Cantidad final',precio_acordado:'Precio acordado',promedio_dias:'Promedio de días',monto_adjudicado:'Monto adjudicado',codigo_articulo:'Código artículo',ganadora:'Ganadora',id_usuario:'Usuario',id_auditoria:'Registro',referencia:'Referencia'})[key]||key.replaceAll('_',' ').replace(/^./,c=>c.toUpperCase());}
function pager(total,size=20){const box=el('div',undefined,'pagination');box.append(el('span',total+' registros · Página '+page+' de '+Math.max(1,Math.ceil(total/size))));const buttons=el('div');const prev=action('Anterior',()=>{page--;load();}),next=action('Siguiente',()=>{page++;load();});prev.disabled=page<=1;next.disabled=page*size>=total;buttons.append(prev,next);box.append(buttons);return box;}
async function all(module){let records=[],p=1;while(true){const data=await api('gestion/'+module+'?pageSize=100&page='+p);records.push(...data.items);if(records.length>=data.total)break;p++;if(p>100)throw Error('Use una búsqueda para reducir los registros');}return records;}
async function init(){
 user=await guateSession(true);if(!user){location.href='login.html';return;}
 $('identity').textContent=user.nombreCompleto+' · '+user.rol;
 $('today').textContent=new Intl.DateTimeFormat('es-GT',{dateStyle:'medium'}).format(new Date());
 MODULES.filter(m=>permission(m[0]).leer).forEach(m=>{const b=action('',()=>navigate(m[0]),'');b.append(el('span',m[3],'nav-symbol'),el('span',m[1]));b.dataset.module=m[0];$('navigation').append(b);});
 $('logout').onclick=async()=>{try{await send('login/logout','POST',{});location.href='login.html';}catch(e){notify(e.message);}};
 $('menu-toggle').onclick=()=>document.querySelector('.sidebar').classList.toggle('open');
 $('close-editor').onclick=$('cancel-editor').onclick=()=>$('editor').close();
 navigate(MODULES.find(m=>permission(m[0]).leer)?.[0]||'dashboard');
}
function navigate(module){active=module;page=1;query='';document.querySelector('.sidebar').classList.remove('open');load();}
async function load(){
 const current=++ticket;clearNotice();const meta=MODULES.find(m=>m[0]===active);$('title').textContent=meta[1];$('breadcrumb').textContent=meta[1];
 $('subtitle').textContent=active==='dashboard'?'Información para decidir y dar seguimiento a las compras.':'Consulte y gestione la información según los permisos de su cuenta.';
 document.querySelectorAll('nav button').forEach(b=>b.classList.toggle('active',b.dataset.module===active));
 $('content').replaceChildren(el('div','Cargando información…','loading'));$('content').setAttribute('aria-busy','true');
 try{
  let view;
  if(active==='dashboard')view=await dashboard();
  else if(active==='ordenes')view=await orders();
  else if(active==='adjudicaciones')view=await awards();
  else if(active==='reportes')view=await reports();
  else if(active==='conexion'){const data=await api('conexion/test');view=el('div');view.append(table([{motor:'SQL Server',estado:data.sqlServer?'Disponible':'Sin conexión',consulta:'SELECT 1',tiempo_ms:data.sqlServerMs},{motor:'PostgreSQL',estado:data.postgres?'Disponible':'Sin conexión',consulta:'SELECT 1',tiempo_ms:data.postgresMs}]));}
  else if(active==='auditoria'){const data=await api('auditoria?page='+page);view=el('div');view.append(table(data.items),pager(data.total,50));}
  else view=await catalog(active);
  if(current===ticket)$('content').replaceChildren(view);
 }catch(e){if(current===ticket){$('content').replaceChildren();notify(e.message);}}
 finally{if(current===ticket)$('content').setAttribute('aria-busy','false');}
}
async function dashboard(){
 const data=await api('dashboard'),view=el('div'),metrics=el('div',undefined,'metrics');
 [['Monto adjudicado',money(data.montoAdjudicado),'Resoluciones registradas','Q'],['Órdenes abiertas',data.ordenesAbiertas,'Recibiendo ofertas','◇'],['Solicitudes de compra',data.pedidos,'Pedidos institucionales','▤'],['Proveedores',data.proveedores,'Red de abastecimiento','♧']].forEach(([title,value,note,icon])=>{const card=el('div',undefined,'metric'),top=el('div',undefined,'metric-top');top.append(el('span',title),el('span',icon,'metric-symbol'));card.append(top,el('div',String(value),'metric-value'),el('div',note,'metric-note'));metrics.append(card);});
 view.append(metrics);const columns=el('div',undefined,'dashboard-columns'),left=el('section',undefined,'panel'),head=el('div',undefined,'panel-heading');head.append(el('h2','Proveedores con mayor monto adjudicado'),el('span','ÚLTIMOS 12 MESES','eyebrow'));left.append(head);const bars=el('div',undefined,'panel-body');const max=Math.max(1,...data.topProveedores.map(r=>Number(r.monto_adjudicado)));
 if(!data.topProveedores.length)bars.append(el('p','Todavía no hay adjudicaciones en este período.'));
 data.topProveedores.forEach(r=>{const row=el('div',undefined,'bar-row'),label=el('div',undefined,'bar-label'),track=el('div',undefined,'bar-track'),fill=el('div',undefined,'bar-fill');label.append(el('span',r.proveedor),el('strong',money(r.monto_adjudicado)));fill.style.width=(Number(r.monto_adjudicado)/max*100)+'%';track.append(fill);row.append(label,track);bars.append(row);});left.append(bars);
 const right=el('section',undefined,'panel'),rh=el('div',undefined,'panel-heading');rh.append(el('h2','Seguimiento de adquisiciones'));right.append(rh);const rb=el('div',undefined,'panel-body');
 [['Solicitudes','Registre artículos, cantidades y fechas necesarias.'],['Órdenes y ofertas',data.ofertas+' ofertas registradas para comparar.'],['Adjudicaciones',data.adjudicaciones+' resoluciones con proveedor y precio final.']].forEach(([t,d],i)=>{const row=el('div',undefined,'workflow-step'),text=el('div');text.append(el('strong',t),el('p',d));row.append(el('span',String(i+1),'step-number'),text);rb.append(row);});rb.append(el('p','Alcance: '+data.alcance));right.append(rb);columns.append(left,right);view.append(columns);return view;
}
async function catalog(module){
 const [schema,data]=await Promise.all([api('gestion/'+module+'/schema'),api('gestion/'+module+'?page='+page+'&q='+encodeURIComponent(query))]);
 const view=el('div'),toolbar=el('div',undefined,'toolbar'),input=el('input');input.placeholder='Buscar en este módulo…';input.value=query;input.setAttribute('aria-label','Buscar registros');
 const search=()=>{query=input.value;page=1;load();};input.addEventListener('keydown',e=>{if(e.key==='Enter')search();});const searchBox=el('div');searchBox.style.display='flex';searchBox.style.gap='8px';searchBox.append(input,action('Buscar',search));toolbar.append(searchBox);
 if(permission(module).crear)toolbar.append(action('+ Nuevo registro',()=>editCatalog(module,schema,null),''));view.append(toolbar);
 const fields=[...schema.claves.filter(k=>!schema.campos.some(f=>f.nombre===k)).map(k=>({nombre:k,etiqueta:'ID'})),...schema.campos.filter(f=>f.tipo!=='password')];
 if(module==='pedidos')fields.push({nombre:'id_orden',etiqueta:'Orden asignada'});
 if(module==='departamentos')fields.push({nombre:'codigo_sucursal',etiqueta:'Código de sucursal'});
 if(module==='proveedorarticulos')fields.push({nombre:'proveedor',etiqueta:'Proveedor'},{nombre:'articulo',etiqueta:'Artículo'});
 if(module==='pedidos')fields.push({nombre:'departamento',etiqueta:'Departamento'},{nombre:'sucursal',etiqueta:'Sucursal'},{nombre:'articulo',etiqueta:'Artículo'});
 if(module==='ofertas')fields.push({nombre:'estado_oferta',etiqueta:'Estado'});
 if(module==='proveedores')fields.push({nombre:'promedio_evaluacion',etiqueta:'Evaluación promedio (1–5)'});
 view.append(table(data.items,fields,item=>{const buttons=[];if(permission(module).actualizar)buttons.push(action('Editar',()=>editCatalog(module,schema,item)));if(permission(module).borrar)buttons.push(action('Borrar',async()=>{if(!confirm('¿Borrar este registro? Las referencias e historial pueden impedirlo.'))return;await send('gestion/'+module+'/'+encodeURIComponent(item._key),'DELETE');await load();notify('Registro eliminado.',true);},'danger small'));return buttons;}),pager(data.total));return view;
}
function openEditor(title,save){$('editor-title').textContent=title;$('editor-fields').replaceChildren();$('editor-error').hidden=true;$('save-editor').hidden=!save;$('save-editor').disabled=false;$('editor-form').onsubmit=async e=>{e.preventDefault();$('save-editor').disabled=true;$('editor-error').hidden=true;try{await save();$('editor').close();await load();notify('Cambios guardados correctamente.',true);}catch(error){$('editor-error').textContent=error.message;$('editor-error').hidden=false;}finally{$('save-editor').disabled=false;}};$('editor').showModal();}
function field(labelText,type,value,required=false){const label=el('label',labelText),input=el(type==='textarea'?'textarea':'input');if(type!=='textarea')input.type=type;input.value=value??'';input.required=required;label.append(input);return [label,input];}
function recordLabel(row){if(row.id_pedido!==undefined)return 'Pedido '+row.id_pedido+' · '+(row.articulo||'Artículo '+row.id_articulo)+' · '+row.cantidad+' unidades';if(row.id_departamento!==undefined)return (row.codigo_sucursal||'Sucursal '+row.id_sucursal)+' · '+row.nombre;if(row.codigo_articulo)return row.codigo_articulo+' · '+row.nombre;if(row.codigo_proveedor)return row.codigo_proveedor+' · '+row.nombre_comercial;return row.nombre_comercial||row.nombre_completo||row.nombre_rol||row.nombre_pantalla||row.nombre||row.codigo_sucursal||String(row._key);}
async function editCatalog(module,schema,record){
 const controls=new Map();let body;
 openEditor(record?'Editar registro':'Nuevo registro',async()=>{body={};for(const [name,input]of controls){const f=schema.campos.find(f=>f.nombre===name);body[name]=f.tipo==='boolean'?input.checked:input.value===''?null:['number','decimal'].includes(f.tipo)?Number(input.value):input.value;}await send('gestion/'+module+(record?'/'+encodeURIComponent(record._key):''),record?'PUT':'POST',body);});
 for(const f of schema.campos){
  let labelNode,input;const current=record?.[f.nombre];
  if(f.tipo==='boolean'){[labelNode,input]=field(f.etiqueta,'checkbox','');input.checked=Boolean(current);}
  else if(f.referencia&&!(f.nombre==='id_proveedor'&&user.rol==='AdminProveedor')){
   labelNode=el('label',f.etiqueta);input=el('select');input.required=f.requerido;input.append(new Option('Seleccione…',''));
   try{for(const row of await all(f.referencia)){const key=Object.keys(row).find(k=>k.startsWith('id_'));input.append(new Option(recordLabel(row),row[key]));}}catch(e){$('editor-error').textContent=e.message;$('editor-error').hidden=false;}
   input.value=current??'';labelNode.append(input);
  }else if(['status','tipoorden','subtipoorden'].includes(f.tipo)){
   labelNode=el('label',f.etiqueta);input=el('select');const values=f.tipo==='status'?['Activo','Inactivo']:f.tipo==='tipoorden'?['Grande','Chica']:['Normal','Urgente'];values.forEach(v=>input.append(new Option(v,v)));input.value=current||values[0];labelNode.append(input);
  }else{
   [labelNode,input]=field(f.etiqueta,f.tipo==='password'?'password':f.tipo==='email'?'email':f.tipo==='date'?'date':['number','decimal'].includes(f.tipo)?'number':'text',current??(f.tipo==='date'?dateOnly():''),f.requerido);
   if(f.maximo)input.maxLength=f.maximo;if(['number','decimal'].includes(f.tipo)){input.min='1';input.step=f.tipo==='decimal'?'0.01':'1';if(f.tipo==='decimal')input.min='0.01';}
   if(f.tipo==='password'){input.autocomplete='new-password';input.placeholder=record?'Deje vacío para conservarla':'Mínimo 12 caracteres';input.required=!record;}
   if(f.nombre==='id_proveedor'&&user.rol==='AdminProveedor'){input.value=user.idProveedor;input.readOnly=true;}
  }
  if(record&&schema.claves.includes(f.nombre)){input.disabled=true;}controls.set(f.nombre,input);$('editor-fields').append(labelNode);
 }
}
async function orders(){
 const data=await api('ordenes?page='+page),view=el('div'),toolbar=el('div',undefined,'toolbar');toolbar.append(el('p','Agrupe solicitudes, compare ofertas y seleccione proveedores.'));if(permission('ordenes').crear)toolbar.append(action('+ Crear orden',()=>editOrder(null),''));view.append(toolbar);
 view.append(table(data.items,['id_orden','descripcion','fecha_creacion','fecha_limite_oferta','tipo','subtipo','estado','pedidos'],item=>{const b=[action('Ver / comparar',()=>showOrder(item.id_orden))];if(permission('ordenes').actualizar)b.push(action('Editar',async()=>editOrder(await api('ordenes/'+item.id_orden))));if(permission('ordenes').borrar)b.push(action('Borrar',async()=>{if(!confirm('¿Borrar la orden y liberar sus solicitudes?'))return;await send('ordenes/'+item.id_orden,'DELETE');await load();},'danger small'));return b;}),pager(data.total));return view;
}
async function editOrder(order){
 const [types,subtypes,pedidos]=await Promise.all([all('tiposorden'),all('subtiposorden'),all('pedidos')]);const selected=new Set(order?.pedidos.map(p=>p.id_pedido)||[]);
 const inputs={};openEditor(order?'Editar orden':'Nueva orden',async()=>{const ids=[...$('editor-fields').querySelectorAll('input[data-pedido]:checked')].map(c=>Number(c.dataset.pedido));await send('ordenes'+(order?'/'+order.id_orden:''),order?'PUT':'POST',{descripcion:inputs.descripcion.value,fecha_creacion:inputs.fecha_creacion.value,fecha_limite_oferta:inputs.fecha_limite_oferta.value,id_tipo:Number(inputs.id_tipo.value),id_subtipo:inputs.id_subtipo.value?Number(inputs.id_subtipo.value):null,pedidoIds:ids});});
 [['descripcion','Descripción','text',order?.descripcion],['fecha_creacion','Fecha creación','date',order?.fecha_creacion||dateOnly()],['fecha_limite_oferta','Límite de ofertas','date',order?.fecha_limite_oferta]].forEach(([key,title,type,value])=>{const [l,i]=field(title,type,value,true);inputs[key]=i;$('editor-fields').append(l);});
 const [notesLabel,notes]=field('Observaciones','text',order?.observaciones||'',false);notes.maxLength=300;inputs.observaciones=notes;$('editor-fields').append(notesLabel);
 const tl=el('label','Tipo de orden'),type=el('select'),sl=el('label','Subtipo'),sub=el('select');types.forEach(t=>type.append(new Option(t.nombre,t.id_tipo)));type.value=order?.id_tipo||types[0].id_tipo;function refresh(){sub.replaceChildren(new Option('Sin subtipo',''));subtypes.filter(s=>s.id_tipo===Number(type.value)).forEach(s=>sub.append(new Option(s.nombre,s.id_subtipo)));}refresh();sub.value=order?.id_subtipo||'';type.onchange=refresh;tl.append(type);sl.append(sub);inputs.id_tipo=type;inputs.id_subtipo=sub;$('editor-fields').append(tl,sl);
 const full=el('div',undefined,'full-width');full.append(el('label','Solicitudes incluidas'));const list=el('div',undefined,'check-list');pedidos.filter(p=>p.id_orden===null||selected.has(p.id_pedido)).forEach(p=>{const l=el('label'),check=el('input');check.type='checkbox';check.dataset.pedido=p.id_pedido;check.checked=selected.has(p.id_pedido);l.append(check,el('span','Pedido '+p.id_pedido+' · Artículo '+p.id_articulo+' · '+p.cantidad+' unidades · solicitado '+p.fecha_solicitud));list.append(l);});full.append(list);$('editor-fields').append(full);
}
async function showOrder(id){
 const order=await api('ordenes/'+id);openEditor('Orden '+id+' · '+order.estado,null);const box=el('div',undefined,'full-width');box.append(el('p',order.descripcion),el('h2','Solicitudes'),table(order.pedidos,['id_pedido','articulo','departamento','cantidad','fecha_necesaria']),el('h2','Comparación de ofertas'),table(order.ofertas,['id_oferta','id_pedido','proveedor','cantidad_solicitada','precio_unitario','diferencia_precio','mejor_precio','empate','fecha_oferta','ganadora']));
 if(!order.adjudicacion&&permission('adjudicaciones').crear)box.append(action('Adjudicar orden',()=>{$('editor').close();editAward(order,null);},''));
 $('editor-fields').append(box);
}
async function awards(){const data=await api('adjudicaciones'),view=el('div');view.append(table(data.items,['id_adjudicacion','id_orden','descripcion','fecha_resolucion','monto'],item=>{const buttons=[action('Ver detalle',async()=>{const award=await api('adjudicaciones/'+item.id_adjudicacion);openEditor('Adjudicación '+item.id_adjudicacion,null);const box=el('div',undefined,'full-width');box.append(table(award.detalles,['id_pedido','articulo','proveedor','cantidad_final','precio_acordado']));$('editor-fields').append(box);})];if(permission('adjudicaciones').actualizar)buttons.push(action('Editar',async()=>{const [o,a]=await Promise.all([api('ordenes/'+item.id_orden),api('adjudicaciones/'+item.id_adjudicacion)]);editAward(o,a);}));if(permission('adjudicaciones').borrar)buttons.push(action('Revertir',async()=>{if(!confirm('¿Revertir la adjudicación completa? Esta acción queda en el historial.'))return;await send('adjudicaciones/'+item.id_adjudicacion,'DELETE');await load();},'danger small'));return buttons;}));return view;}
function editAward(order,award){
 const lines=[],inputs={};openEditor(award?'Editar adjudicación':'Adjudicar orden '+order.id_orden,async()=>{const details=lines.map(line=>({id_pedido:line.pedido,id_oferta:Number(line.offer.value),cantidad_final:Number(line.quantity.value),precio_acordado:Number(line.price.value)}));await send('adjudicaciones'+(award?'/'+award.id_adjudicacion:''),award?'PUT':'POST',{id_orden:order.id_orden,fecha_resolucion:inputs.date.value,observaciones:inputs.notes.value,detalles:details});});
 const [labelNode,date]=field('Fecha de resolución','date',award?.fecha_resolucion||dateOnly(),true);inputs.date=date;$('editor-fields').append(labelNode);
 const [notesLabel,notes]=field('Observaciones','text',award?.observaciones||'',false);notes.maxLength=300;inputs.notes=notes;$('editor-fields').append(notesLabel);
 const full=el('div',undefined,'full-width');full.append(el('p','Seleccione una oferta para cada solicitud y confirme cantidad y precio final.'));
 order.pedidos.forEach(p=>{const existing=award?.detalles.find(d=>d.id_pedido===p.id_pedido),row=el('div',undefined,'award-line');row.append(el('strong',p.articulo+' · Pedido '+p.id_pedido+' · '+p.cantidad+' unidades solicitadas'));const offerLabel=el('label','Oferta ganadora'),offer=el('select');offer.required=true;offer.append(new Option('Seleccione una oferta…',''));order.ofertas.filter(f=>f.id_pedido===p.id_pedido).forEach(f=>offer.append(new Option(f.proveedor+' · '+money(f.precio_unitario),f.id_oferta)));offer.value=existing?.id_oferta||'';offerLabel.append(offer);const [ql,quantity]=field('Cantidad final','number',existing?.cantidad_final||p.cantidad,true),[pl,price]=field('Precio acordado (Q)','number',existing?.precio_acordado||'',true);quantity.min=1;quantity.max=p.cantidad;quantity.step=1;price.min=.01;price.step=.01;price.readOnly=true;offer.onchange=()=>{price.value=order.ofertas.find(f=>f.id_oferta===Number(offer.value))?.precio_unitario||'';};row.append(offerLabel,ql,pl);full.append(row);lines.push({pedido:p.id_pedido,offer,quantity,price});});$('editor-fields').append(full);
}
async function reports(){
 const catalog=await api('reportes'),view=el('div'),filters=el('form',undefined,'report-filters'),selector=el('select'),reportLabel=el('label','Informe académico');catalog.informes.forEach(r=>{if(user.rol!=='AdminProveedor'||![4,6,7].includes(r.id))selector.append(new Option(r.id+'. '+r.nombre,r.id));});reportLabel.append(selector);filters.append(reportLabel);const controls={};
 [['articulo','Artículo (ID; 0 = todos)','number',0],['proveedor','Proveedor (ID; 0 = todos)','number',0],['orden','Orden (ID)','number',''],['sucursal','Sucursal (ID; 0 = todas)','number',0],['anio','Año','number',new Date().getFullYear()],['desde','Desde','date',dateOnly(new Date(new Date().setFullYear(new Date().getFullYear()-1)))],['hasta','Hasta','date',dateOnly()]].forEach(([key,title,type,value])=>{const [l,i]=field(title,type,value);controls[key]={label:l,input:i};filters.append(l);});
 const submit=el('button','Consultar');submit.type='submit';filters.append(submit);view.append(filters);const results=el('div');view.append(results);
 function showFields(){const parameters=catalog.informes.find(r=>r.id===Number(selector.value)).parametros;for(const [key,c]of Object.entries(controls))c.label.hidden=!parameters.includes(key);}
 selector.onchange=showFields;showFields();
 if(!catalog.ssrsConfigurado)view.append(el('div','Los ocho informes SSRS están preparados. Su servidor de informes todavía no está configurado. Las consultas de esta pantalla usan datos reales de la institución.','help-note'));
 filters.onsubmit=async e=>{e.preventDefault();clearNotice();submit.disabled=true;try{const params=new URLSearchParams();const metadata=catalog.informes.find(r=>r.id===Number(selector.value));metadata.parametros.forEach(key=>{if(controls[key].input.value)params.set(key,controls[key].input.value);});const data=await api('reportes/'+selector.value+'?'+params);results.replaceChildren(el('h2',data.nombre),el('p',data.total+' registros'),table(data.items));const toolbar=el('div',undefined,'toolbar');toolbar.append(action('Imprimir',()=>window.print()));toolbar.append(action('Exportar CSV',()=>csv(data.items,'GuateCompras-Informe-'+selector.value+'.csv')));if(catalog.ssrsConfigurado&&user.rol!=='AdminProveedor'){toolbar.append(action('Abrir SSRS',()=>{const names=['01_HistorialCompra','02_TopProveedores','03_ComparacionOfertas','04_PedidosSinAsignar','05_OrdenesAbiertas','06_GastoDepartamento','07_PromedioAdjudicacion','08_EvolucionPrecios'];const url=new URL(catalog.ssrsUrl);if(!['http:','https:'].includes(url.protocol))throw Error('URL SSRS no válida');url.search='/GuateCompras/'+names[Number(selector.value)-1]+'&rs:Command=Render';const mapping={articulo:'Articulo',proveedor:'Proveedor',orden:'Orden',sucursal:'Sucursal',anio:'Anio',desde:'Desde',hasta:'Hasta'};for(const [k,v]of params)url.search+='&'+mapping[k]+'='+encodeURIComponent(v);window.open(url.href,'_blank','noopener');}));}results.prepend(toolbar);}catch(error){notify(error.message);}finally{submit.disabled=false;}};
 return view;
}
function csv(items,name){if(!items.length)throw Error('No hay datos para exportar');const keys=Object.keys(items[0]);const escape=value=>{let s=value==null?'':String(value);if(/^[=+@-]/.test(s))s="'"+s;return '"'+s.replaceAll('"','""')+'"';};const content=[keys.map(label).map(escape).join(','),...items.map(r=>keys.map(k=>escape(r[k])).join(','))].join('\r\n');const url=URL.createObjectURL(new Blob(['\uFEFF'+content],{type:'text/csv;charset=utf-8'}));const a=el('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
init().catch(e=>notify(e.message));
