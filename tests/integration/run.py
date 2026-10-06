"""Suite real contra el servidor iniciado y la semilla. No utiliza mocks."""
import subprocess,sys
from pathlib import Path
for name in ['security','crud','acquisitions','reports','edge_cases']:
 print('\n=== '+name+' ===',flush=True)
 subprocess.run([sys.executable,str(Path(__file__).with_name('test_'+name+'.py'))],check=True)
print('PASS SUITE HTTP COMPLETA')
