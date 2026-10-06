/* Compartido por las pantallas existentes y los clientes nuevos. */
(() => {
  const originalFetch = window.fetch.bind(window);
  window.guateRow = (row, values, edit, remove) => {
    row.replaceChildren();
    values.forEach(value => { const cell=document.createElement('td');cell.textContent=value??'';row.append(cell); });
    const cell=document.createElement('td');
    [['Editar',edit],['Eliminar',remove]].forEach(([label,callback])=>{const button=document.createElement('button');button.type='button';button.textContent=label;button.addEventListener('click',callback);cell.append(button);});
    row.append(cell);
  };
  let sessionPromise;
  window.guateSession = async (refresh = false) => {
    if (refresh || !sessionPromise) {
      sessionPromise = originalFetch('api/login/me', { credentials: 'same-origin' })
        .then(async response => response.ok ? response.json() : null);
    }
    return sessionPromise;
  };
  window.fetch = async (input, options = {}) => {
    const url = typeof input === 'string' ? input : input.url;
    const parsed = new URL(url, location.href);
    const method = (options.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
    if (parsed.origin === location.origin && parsed.pathname.includes('/api/') &&
        !['GET', 'HEAD', 'OPTIONS'].includes(method) && !parsed.pathname.endsWith('/api/login')) {
      const user = await window.guateSession();
      if (user) {
        const headers = new Headers(options.headers || (input instanceof Request ? input.headers : {}));
        headers.set('X-CSRF-Token', user.csrf);
        options = { ...options, headers };
      }
    }
    return originalFetch(input, { credentials: 'same-origin', ...options });
  };
})();
