<#include "header.ftl">
<h1>${title}</h1>
<div class="chat-container">
  <div class="messages" id="messages">
    <#if messages?? && messages?size gt 0>
      <#list messages as msg>
        <div class="message" id="msg-${msg.id}">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; font-size: 0.9em; color: #555;">
            <strong>${msg.sender}</strong>
            <div style="display: flex; gap: 6px; align-items: center;">
              <span class="date">
                <#if msg.updated_at??>
                  ${msg.updated_at} <em style="font-size:0.85em; color:gray;">(modifié)</em>
                <#else>
                  ${msg.created_at}
                </#if>
              </span>
              <button class="btn-edit" onclick="startEdit(${msg.id})">✏️</button>
              <button class="btn-edit" onclick="deleteMessage(${msg.id})">✏Delete</button>
            </div>
          </div>
          <div class="msg-content" style="margin-top: 2px;">${msg.content}</div>

          <#-- Formulaire d'édition -->
          <div class="edit-form" id="edit-${msg.id}" style="display:none; margin-top: 6px;">
            <textarea class="edit-textarea" id="edit-text-${msg.id}">${msg.content}</textarea>
            <div style="display:flex; gap:6px; margin-top:4px;">
              <button class="btn-save" onclick="saveEdit(${msg.id})">Sauvegarder</button>
              <button class="btn-cancel" onclick="cancelEdit(${msg.id})">Annuler</button>
            </div>
          </div>
        </div>
      </#list>
    <#else>
      <p>No messages yet 👀</p>
    </#if>
  </div>
  <hr/>
  <div class="chat-form">
    <form action="/api/messages" method="post">
      <div>
        <label for="sender">Name:</label>
        <input type="text" id="sender" name="sender" required>
      </div>
      <div>
        <label for="message">Message:</label>
        <textarea id="message" name="content" required></textarea>
      </div>
      <div>
        <button type="submit">Send</button>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/vertx3-eventbus-client/vertx-eventbus.min.js"></script>

<#noparse>
<script>
  // On initialise l'EventBus
  const eb = new EventBus("http://localhost:8080/eventbus");

  // Ta fonction de formatage de date
  function getFormattedDate() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    return y + "/" + m + "/" + d + " " + hh + ":" + mm;
  }

  // Fonction pour faire défiler vers le bas (travail de ton ami)
  function scrollToBottom() {
    const container = document.getElementById('messages');
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }

  eb.onopen = function () {
    console.log("Connected to EventBus");

    // RÉCEPTION NOUVEAU MESSAGE (POST)
    eb.registerHandler("chat.message", function (error, message) {
      const msg = message.body;
      const container = document.getElementById("messages");
      const div = document.createElement("div");
      div.className = "message";
      if (msg.id) div.id = "msg-" + msg.id;

      div.innerHTML = `
        <div style="display: flex; justify-content: space-between; font-size: 0.9em; color: #555;">
          <strong>${msg.sender}</strong>
          <span class="date">${getFormattedDate()}</span>
        </div>
        <div class="msg-content" style="margin-top: 2px;">${msg.content}</div>
      `;
      container.appendChild(div);
      scrollToBottom();
    });

    // RÉCEPTION MODIFICATION (PUT)
    eb.registerHandler("chat.update", function (error, message) {
      const data = message.body;
      const msgDiv = document.getElementById("msg-" + data.id);
      if (msgDiv) {
        msgDiv.querySelector(".msg-content").textContent = data.content;
        const dateSpan = msgDiv.querySelector(".date");
        if (dateSpan && data.updated_at) {
          dateSpan.innerHTML = data.updated_at + ' <em style="font-size:0.85em; color:gray;">(modifié)</em>';
        }
      }
    });
  };

  // FONCTIONS D'INTERFACE (Ton travail)
  function startEdit(id) {
    const el = document.getElementById("edit-" + id);
    if (el) el.style.display = "block";
  }

  function cancelEdit(id) {
    const el = document.getElementById("edit-" + id);
    if (el) el.style.display = "none";
  }

  async function saveEdit(id) {
    const textarea = document.getElementById("edit-text-" + id);
    const content = textarea ? textarea.value.trim() : "";
    if (!content) return;

    try {
      await fetch("/api/messages", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: id, content: content })
      });
      cancelEdit(id);
    } catch (err) {
      console.error("Erreur sauvegarde:", err);
    }
  }
  async function deleteMessage(id) {
    msg = document.getElementById("msg-"+id);
    try {
      await fetch("/api/messages", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: id, })

      });
      msg.style.display = "none"
    } catch (err) {
      console.error("Erreur de suppresion:", err);
    }
  }

  // Au chargement initial
  window.addEventListener('load', scrollToBottom);
</script>
</#noparse>

<#include "footer.ftl">
