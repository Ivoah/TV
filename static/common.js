$(() => {
  $("form").on("submit", e => {
    e.preventDefault();
    const form = $(e.target);
    $.ajax(form.attr("action"), {
      method: form.attr("method"),
      data: form.serialize(),
      success: () => location.reload()
    });
  });
});
