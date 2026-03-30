<#include "header.ftl">
<h1>${title}</h1>
<div class="chat-container">
  <div class="messages" id="messages">
    <#if messages?? && messages?size gt 0>
      <#list messages as msg>
        <div class="message">
          <div class="message">
            <div style="display: flex; justify-content: space-between; font-size: 0.9em; color: #555;">
              <strong>${msg.sender}</strong>
              <span class="date">${msg.created_at}</span>
            </div>
            <div style="margin-top: 2px;">
              ${msg.content}
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
  const eb = new EventBus("http://localhost:8080/eventbus");

  eb.onopen = function () {

    console.log("Connected to EventBus");

    eb.registerHandler("chat.message", function (error, message) {
      const msg = message.body;

      // Create HTML for new message
      const container = document.getElementById("messages");

      const div = document.createElement("div");
      div.className = "message";

      div.innerHTML = `
        <div style="display: flex; justify-content: space-between; font-size: 0.9em; color: #555;">
          <strong>${msg.sender}</strong>
          <span class="date">${new Date().toLocaleString()}</span>
        </div>
        <div style="margin-top: 2px;">
          ${msg.content}
        </div>
      `;

      container.appendChild(div);

      // Auto scroll to bottom
      container.scrollTop = container.scrollHeight;
    });

  };
</script>
</#noparse>
<#include "footer.ftl">
