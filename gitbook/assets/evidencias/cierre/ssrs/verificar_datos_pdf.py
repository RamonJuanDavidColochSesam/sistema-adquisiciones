from pathlib import Path
import json,re,hashlib,unicodedata
from decimal import Decimal
from datetime import datetime
import pdfplumber
p=Path(__file__).resolve().parent
b=json.loads((p/'baseline-consultas.json').read_text(encoding='utf8'))
pub=json.loads((p/'publicacion-real.json').read_text(encoding='utf-8-sig'))
def norm(s):
 return re.sub(r'\s+',' ',unicodedata.normalize('NFC',s or '')).strip()
results=[]
for n,entry in enumerate(pub['informes'],1):
 f=p/(entry['nombre']+'.pdf')
 assert hashlib.sha256(f.read_bytes()).hexdigest()==entry['sha256PDF']
 assert entry['sha256Local']==entry['sha256Servidor']
 rows=[]
 with pdfplumber.open(f) as d:
  for page in d.pages:
   tables=page.extract_tables()
   assert len(tables)==1,(n,'tabla')
   rows.extend(tables[0][1:])
  pages=len(d.pages)
 expected=b['informes'][str(n)]['items']
 assert len(rows)==len(expected),(n,len(rows),len(expected))
 for i,(cells,record) in enumerate(zip(rows,expected)):
  assert len(cells)==len(record)
  for (key,value),cell in zip(record.items(),cells):
   cell=norm(cell)
   if isinstance(value,bool):assert cell==('GANADORA' if value else ''),(n,i,key,cell)
   elif value is None:assert cell=='',(n,i,key)
   elif isinstance(value,(int,float)):assert Decimal(cell)==Decimal(str(value)),(n,i,key,cell,value)
   elif re.match(r'^\d{4}-\d{2}-\d{2}$',str(value)):
    assert datetime.strptime(cell,'%d/%m/%Y %H:%M:%S').date().isoformat()==value,(n,i,key)
   else:assert cell==norm(value),(n,i,key,cell,value)
 results.append({'informe':entry['nombre'],'filas':len(rows),'paginas':pages,'datos':'COINCIDEN TODOS LOS CAMPOS','rdl':'SHA256 IDENTICO AL SERVIDOR','pdfSha256':entry['sha256PDF'],'revisionVisual':'REVISADAS TODAS LAS PAGINAS, SIN RECORTES NI SOLAPAMIENTOS'})
 print('PASS',entry['nombre'],len(rows),'filas',pages,'paginas')
(p/'verificacion-datos.json').write_text(json.dumps({'fecha':'2026-10-06','orden':b['orden'],'desde':b['desde'],'hasta':b['hasta'],'informes':results,'limite':'Comparacion de los parametros registrados; no acredita pruebas de todas las combinaciones de filtros.'},ensure_ascii=False,indent=2),encoding='utf8')
pub['revisionVisualPendiente']=False
(p/'publicacion-real.json').write_text(json.dumps(pub,ensure_ascii=False,indent=2),encoding='utf8')
