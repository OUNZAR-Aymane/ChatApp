<#include "header.ftl">
<h1>${title}</h1>
<div class="chat-container">
  <div class="messages">
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
<#include "footer.ftl">
