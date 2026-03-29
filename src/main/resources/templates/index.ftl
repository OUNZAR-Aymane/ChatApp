<#include "header.ftl">
<h1>${title}</h1>
<div class="chat-container">
  <div class="messages">
    <#if messages?? && messages?size gt 0>
      <#list messages as msg>
        <div class="message">
          <strong>${msg.sender}</strong>: ${msg.content}
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
