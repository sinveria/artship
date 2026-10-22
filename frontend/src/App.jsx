import { useEffect, useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export default function App() {
  const [backendStatus, setBackendStatus] = useState('Проверка...');

  useEffect(() => {
    fetch(`${API_BASE_URL}/api/health`)
      .then((response) => {
        if (!response.ok) throw new Error('Backend вернул ошибку');
        return response.json();
      })
      .then((data) => setBackendStatus(data.status === 'UP' ? 'Подключён' : 'Недоступен'))
      .catch(() => setBackendStatus('Недоступен'));
  }, []);

  return (
    <main className="page">
      <section className="panel">
        <p className="eyebrow">STARTER PROJECT</p>
        <h1>ArtShip</h1>
        <p className="description">
          Базовая структура frontend и backend создана. Здесь можно начинать
          добавлять компоненты, API и бизнес-логику.
        </p>
        <div className="status-row">
          <span className={`status-dot ${backendStatus === 'Подключён' ? 'online' : ''}`} />
          <span>Backend:</span>
          <strong>{backendStatus}</strong>
        </div>
      </section>
    </main>
  );
}
