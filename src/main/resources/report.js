function stackTraceFor() {
  var stackId = this.getAttribute("data-stack-id");
  return document.getElementById(stackId).innerHTML;
}
$(function () {
  $('body').popover({
    selector: '[data-toggle="popover"]',
    content: stackTraceFor,
    html: true
  });
});