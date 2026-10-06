from pathlib import Path
import urllib.request, urllib.error, http.cookiejar, json, time
import os
root=Path(__file__).resolve().parents[2]
credentials={}
for line in (root/'config/demo-access.properties').read_text().splitlines():
 if line and not line.startswith('#') and '=' in line:
  k,v=line.split('=',1);credentials[k]=v
base=os.getenv('GUATECOMPRAS_TEST_URL','http://127.0.0.1:18080/sistema-adquisiciones/').rstrip('/')+'/'
def client():
 return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
def call(opener,path,method='GET',body=None,csrf=None):
 headers={'Content-Type':'application/json'}
 if csrf:headers['X-CSRF-Token']=csrf
 request=urllib.request.Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
 try:
  with opener.open(request,timeout=25) as r:return r.status,json.loads(r.read() or b'null'),r.headers
 except urllib.error.HTTPError as e:
  raw=e.read()
  try: value=json.loads(raw)
  except ValueError:value=None
  return e.code,value,e.headers
def expect(actual,wanted,label):
 assert actual==wanted,f'{label}: expected {wanted}, got {actual}'
 print('PASS',label,actual)
