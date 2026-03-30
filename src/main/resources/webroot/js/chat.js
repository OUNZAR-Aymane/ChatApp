const eb = new EventBus("http://localhost:8080/eventbus");

eb.onopen = function () {
  console.log("Connected to EventBus");

  eb.registerHandler("chat.message", function (error, message) {
    const msg = message.body;

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
    container.scrollTop = container.scrollHeight;
  });
};
