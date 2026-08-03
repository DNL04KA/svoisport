(function () {
  'use strict';

  const loading = document.getElementById('activationLoading');
  const confirm = document.getElementById('activationConfirm');
  const success = document.getElementById('activationSuccess');
  const error = document.getElementById('activationError');
  const errorMessage = document.getElementById('activationErrorMessage');
  const form = document.getElementById('activationForm');
  const submit = document.getElementById('activationSubmit');
  const sessionId = new URLSearchParams(window.location.search).get('session') || '';
  let selectedPlan = null;

  function show(target) {
    [loading, confirm, success, error].forEach((element) => { element.hidden = element !== target; });
  }

  function showError(message) {
    errorMessage.textContent = message;
    show(error);
  }

  async function requestJson(url, options) {
    const response = await fetch(url, options);
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      const requestError = new Error(data.error || `HTTP ${response.status}`);
      requestError.status = response.status;
      requestError.data = data;
      throw requestError;
    }
    return data;
  }

  async function loadSession() {
    if (!sessionId) {
      showError('В ссылке отсутствует идентификатор сессии. Отсканируйте QR-код ещё раз.');
      return;
    }
    try {
      const data = await requestJson(`/api/activation-session.php?session=${encodeURIComponent(sessionId)}`);
      selectedPlan = data.plan || null;
      if (selectedPlan) {
        document.getElementById('activationKicker').textContent = 'Оформление подписки';
        document.getElementById('activationHeading').textContent = 'Подтвердите выбранный тариф';
        document.getElementById('activationDescription').textContent = 'Оплата ниже является тестовой и не списывает деньги.';
        document.getElementById('activationPlanTitle').textContent = selectedPlan.title;
        document.getElementById('activationPlanPrice').textContent = selectedPlan.price;
        document.getElementById('activationPlanTotal').textContent = selectedPlan.total ? `Итого ${selectedPlan.total}` : '';
        document.getElementById('activationPlan').hidden = false;
        submit.textContent = 'Оплатить и активировать';
      }
      if (data.status === 'activated') show(success);
      else if (data.status === 'expired') showError('Время действия QR-кода истекло. Создайте новый код на телевизоре.');
      else show(confirm);
    } catch (requestError) {
      showError(requestError.status === 404
        ? 'Сессия не найдена. Отсканируйте актуальный QR-код с экрана телевизора.'
        : 'Не удалось проверить сессию. Проверьте подключение и обновите страницу.');
    }
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!form.reportValidity()) return;
    submit.disabled = true;
    submit.textContent = selectedPlan ? 'Обрабатываем оплату…' : 'Активируем…';
    try {
      const data = await requestJson('/api/activate-session.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session: sessionId,
          email: document.getElementById('activationEmail').value.trim(),
        }),
      });
      if (data.status === 'activated') show(success);
      else showError('QR-код уже истёк. Создайте новый код на телевизоре.');
    } catch (requestError) {
      showError(requestError.status === 410
        ? 'Время действия QR-кода истекло. Создайте новый код на телевизоре.'
        : 'Не удалось активировать телевизор. Попробуйте ещё раз.');
    } finally {
      submit.disabled = false;
      submit.textContent = selectedPlan ? 'Оплатить и активировать' : 'Активировать телевизор';
    }
  });

  loadSession();
}());
