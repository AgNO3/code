
PrimeFaces.widget.OverlayPanel.prototype.applyFocus = function() {
	if (!this.jq.is('.ui-no-autofocus')) {
		this.jq.find(':not(:submit):not(:button):input:visible:enabled:first')
				.focus();
	}
}
