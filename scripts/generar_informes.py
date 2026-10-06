"""Genera ocho RDL SSRS 2016 desde las mismas consultas verificadas de la API.
Solo usa la biblioteca estándar. No publica ni contiene contraseñas."""
from pathlib import Path
import re, xml.etree.ElementTree as E
ROOT=Path(__file__).resolve().parent.parent
DEST=ROOT/'reports/ssrs';DEST.mkdir(parents=True,exist_ok=True)
SQLDEST=ROOT/'db/queries';SQLDEST.mkdir(parents=True,exist_ok=True)
source=(ROOT/'src/main/java/com/adquisiciones/servicio/ReportesServicio.java').read_text(encoding='utf-8')
queries={int(k):v.replace('%SCOPE%','') for k,v in re.findall(r'^\s*(\d+),"([^"\n]+)"',source,re.M)}
REPORTS={
1:('HistorialCompra',['codigo_articulo','articulo','proveedor','fecha_resolucion','cantidad_final','precio_acordado','monto'],['Articulo','Articulo','Desde','Hasta']),
2:('TopProveedores',['codigo_proveedor','proveedor','monto_adjudicado','ordenes'],['Desde','Hasta']),
3:('ComparacionOfertas',['id_pedido','articulo','cantidad','proveedor','precio_unitario','fecha_oferta','ganadora','cantidad_final','precio_acordado'],['Orden']),
4:('PedidosSinAsignar',['id_pedido','codigo_sucursal','departamento','articulo','cantidad','fecha_solicitud','fecha_necesaria'],[]),
5:('OrdenesAbiertas',['id_orden','descripcion','fecha_creacion','fecha_limite_oferta','tipo','subtipo'],[]),
6:('GastoDepartamento',['codigo_sucursal','ciudad','departamento','gasto'],['Sucursal','Sucursal','InicioAnio','FinAnio']),
7:('PromedioAdjudicacion',['ordenes_adjudicadas','promedio_dias'],['Desde','Hasta']),
8:('EvolucionPrecios',['codigo_articulo','articulo','proveedor','fecha_oferta','precio_unitario','id_orden'],['Articulo','Articulo','Proveedor','Proveedor','Desde','Hasta'])}
NS='http://schemas.microsoft.com/sqlserver/reporting/2016/01/reportdefinition'
E.register_namespace('',NS)
def node(parent,name,text=None,**attrs):
 item=E.SubElement(parent,'{'+NS+'}'+name,attrs);item.text=text;return item
def textbox(parent,name,value,height='0.3in',bold=False):
 box=node(parent,'Textbox',Name=name);node(box,'CanGrow','true');node(box,'KeepTogether','true')
 run=node(node(node(node(node(box,'Paragraphs'),'Paragraph'),'TextRuns'),'TextRun'),'Value',value)
 style=node(run.getparent(),'Style') if hasattr(run,'getparent') else None
 # ElementTree has no parent accessor: text-run style is created explicitly below.
 tr=box.find('.//{'+NS+'}TextRun');sty=node(tr,'Style');node(sty,'FontFamily','Segoe UI');node(sty,'FontSize','8pt')
 if bold:node(sty,'FontWeight','Bold')
 sty=node(box,'Style');border=node(sty,'Border');node(border,'Style','Solid');node(border,'Color','#DCE3EC');node(sty,'PaddingLeft','4pt');node(sty,'PaddingRight','4pt');node(sty,'PaddingTop','4pt');node(sty,'PaddingBottom','4pt')
 if bold:node(sty,'BackgroundColor','#173E5C');node(sty,'Color','White')
 return box
for report,(name,fields,parameters) in REPORTS.items():
 query=queries[report]
 for parameter in parameters:query=query.replace('?', '@'+parameter,1)
 assert '?' not in query
 rpt=E.Element('{'+NS+'}Report')
 node(rpt,'Description','GuateCompras • Informe académico '+str(report)+' • Datos SQL Server')
 sources=node(rpt,'DataSources');ds=node(sources,'DataSource',Name='GuateComprasSQL');node(ds,'DataSourceReference','/GuateCompras/GuateComprasSQL')
 datasets=node(rpt,'DataSets');dataset=node(datasets,'DataSet',Name='Datos');q=node(dataset,'Query');node(q,'DataSourceName','GuateComprasSQL')
 unique=list(dict.fromkeys(parameters))
 if unique:
  qps=node(q,'QueryParameters')
  for parameter in unique:
   qp=node(qps,'QueryParameter',Name='@'+parameter)
   expr='=DateSerial(Parameters!Anio.Value,1,1)' if parameter=='InicioAnio' else '=DateSerial(Parameters!Anio.Value+1,1,1)' if parameter=='FinAnio' else '=Parameters!'+parameter+'.Value'
   node(qp,'Value',expr)
 node(q,'CommandText',query)
 fs=node(dataset,'Fields')
 for field in fields:
  f=node(fs,'Field',Name=field);node(f,'DataField',field)
 visible=[p for p in unique if p not in ('InicioAnio','FinAnio')]
 if report==6:visible.append('Anio')
 if visible:
  rps=node(rpt,'ReportParameters')
  for parameter in visible:
   p=node(rps,'ReportParameter',Name=parameter);node(p,'DataType','DateTime' if parameter in ('Desde','Hasta') else 'Integer')
   if parameter!='Orden':
    value='=DateAdd("yyyy",-1,Today())' if parameter=='Desde' else '=Today()' if parameter=='Hasta' else '=Year(Today())' if parameter=='Anio' else '0'
    node(node(node(p,'DefaultValue'),'Values'),'Value',value)
   node(p,'Prompt',parameter)
 sections=node(rpt,'ReportSections');section=node(sections,'ReportSection');body=node(section,'Body');items=node(body,'ReportItems')
 title=textbox(items,'Titulo','GuateCompras | '+re.sub(r'(?<!^)([A-Z])',r' \1',name),bold=True)
 node(title,'Top','0in');node(title,'Left','0in');node(title,'Height','0.45in');node(title,'Width','10.5in')
 table=node(items,'Tablix',Name='TablaDatos');tb=node(table,'TablixBody');cols=node(tb,'TablixColumns')
 width=10.5/len(fields)
 for field in fields:node(node(cols,'TablixColumn'),'Width',f'{width:.4f}in')
 rows=node(tb,'TablixRows')
 for header in (True,False):
  row=node(rows,'TablixRow');node(row,'Height','0.35in');cells=node(row,'TablixCells')
  for field in fields:
   contents=node(node(cells,'TablixCell'),'CellContents')
   value=field.replace('_',' ').capitalize() if header else '=Fields!'+field+'.Value'
   if not header and field=='ganadora':value='=IIF(Fields!ganadora.Value,"GANADORA","")'
   textbox(contents,('Cab_' if header else 'Dato_')+field,value,bold=header)
 hierarchy=node(table,'TablixColumnHierarchy');members=node(hierarchy,'TablixMembers')
 for field in fields:node(members,'TablixMember')
 hierarchy=node(table,'TablixRowHierarchy');members=node(hierarchy,'TablixMembers')
 header=node(members,'TablixMember');node(header,'KeepWithGroup','After');node(header,'RepeatOnNewPage','true');node(header,'FixedData','true')
 detail=node(members,'TablixMember');node(detail,'Group',Name='Detalle')
 node(table,'DataSetName','Datos');node(table,'Top','0.75in');node(table,'Left','0in');node(table,'Height','0.7in');node(table,'Width','10.5in');node(table,'Style')
 node(body,'Height','1.5in');node(body,'Style');node(section,'Width','10.5in');page=node(section,'Page')
 for key,val in [('PageHeight','8.3in'),('PageWidth','11.7in'),('LeftMargin','0.5in'),('RightMargin','0.5in'),('TopMargin','0.5in'),('BottomMargin','0.5in')]:node(page,key,val)
 node(page,'Style');node(rpt,'Language','es-GT');node(rpt,'ConsumeContainerWhitespace','true')
 tree=E.ElementTree(rpt);E.indent(tree,space='  ');tree.write(DEST/(f'{report:02d}_'+name+'.rdl'),encoding='utf-8',xml_declaration=True)
 (SQLDEST/(f'{report:02d}_'+name+'.sql')).write_text('-- Parámetros: '+', '.join(unique)+'\n'+query+';\n',encoding='utf-8')
 print('Generado',report,name,'(publicación SSRS pendiente)')
