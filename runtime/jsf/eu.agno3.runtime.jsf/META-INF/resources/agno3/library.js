if (!window.onerror) {
	window.onerror = function(msg, url, line, col, error) {
		alert("Unhandled JavaScript error, please reload the page:\n" + msg
				+ "\nscript: " + url + "\nline: " + line
				+ (col ? ' column: ' + col : '')
				+ (error ? '\nerror: ' + error : ''));
		return true;
	};
}

if (!console) {
	console = {};
}

if (!console.log) {
	console.log = function(x) {};
}

function widgetFromJq(elem) {
	var id = elem.attr('id');
	if (!id) {
		return undefined;
	}
	var defWidgetVar = "widget_" + id.replace(/:/g, "_");
	if ( PrimeFaces.widgets[defWidgetVar]) {
		return PrimeFaces.widgets[defWidgetVar];
	}
	for (var propertyName in PrimeFaces.widgets) {
		if (PrimeFaces.widgets[propertyName].id === id) {
    	 	return PrimeFaces.widgets[propertyName];
     	}
	}
}

// copied from primefaces SVN core/core.dialog.js
var AgNO3DialogOverlay = {

	openDialog : function(cfg) {
		var dialogId = cfg.sourceComponentId + '_dlg';
		if (document.getElementById(dialogId)) {
			console.log("Dialog component already exists");
			return;
		}

		var doc = $(document);
		console.log("Open dialog");
		doc.trigger('pfAjaxDelay');

		var dialogWidgetVar = cfg.sourceComponentId.replace(/:/g, '_')
				+ '_dlgwidget', dialogDOM = $(
				'<div id="'
						+ dialogId
						+ '" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow ui-overlay-hidden"'
						+ ' data-pfdlgcid="' + cfg.pfdlgcid
						+ '" data-widgetvar="' + dialogWidgetVar + '"></div>')
				.append(
						'<div class="ui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top"><span class="ui-dialog-title"></span></div>');

		if (cfg.options.closable !== false) {
			dialogDOM
					.children('.ui-dialog-titlebar')
					.append(
							'<a class="ui-dialog-titlebar-icon dialog-close ui-corner-all" style="float: right; margin: 0px; top:0px; padding: 1px; cursor:pointer; border: 0;" href="#" role="button"><span class="ui-icon ui-icon-closethick"></span></a>');
		}

		dialogDOM
				.append('<div class="ui-dialog-content ui-widget-content" style="height: auto;">'
						+ '<iframe style="border:0 none" frameborder="0" width="100%" scrolling="auto" />'
						+ '</div>');

		dialogDOM.appendTo(document.body);

		var widget = this;

		var browserWidth = $(window).width();
		if (cfg.options.maxContentWidth) {
			browserWidth = Math.min(browserWidth, cfg.options.maxContentWidth);
		}

		var dialogFrame = dialogDOM.find('iframe'), frameURL = cfg.url, frameWidth = cfg.options.contentWidth
				|| Math.round(browserWidth * 2 / 3);

		dialogFrame.width(frameWidth);

		var left = Math.round((browserWidth - frameWidth) / 2);
		var top = Math.round($(window).height() * 0.1);
		var posStr = left + "," + top;

		var dialogCloseButton = dialogDOM
				.find('.ui-dialog-titlebar .dialog-close');
		dialogCloseButton.hide();

		dialogFrame.on('load', function() {
			var $frame = $(this), titleElement = $frame.contents()
					.find('title');

			if (!$frame.data('initialized')) {
				PrimeFaces.cw('DynamicDialog', dialogWidgetVar, {
					id : dialogId,
					position : posStr,
					sourceComponentId : cfg.sourceComponentId,
					sourceWidget : cfg.sourceWidget,
					onHide : function() {
						this.jq.remove();
						PF[dialogWidgetVar] = undefined;
					},
					modal : true,
					resizable : false,
					draggable : true,
					closable : cfg.options.closable,
					width : cfg.options.width,
					height : cfg.options.height
				});
			}

			if (titleElement.length > 0) {
				PF(dialogWidgetVar).titlebar.children('span.ui-dialog-title')
						.html(titleElement.text());
			}

			var closeable = $frame.contents().find('input[name=closable]')
					.val();
			PF(dialogWidgetVar).curId = $frame.contents().find(
					'input[name=returnTo]').val();

			if (closeable) {
				dialogCloseButton.show();
				dialogCloseButton.prop('disabled', false);
				if (PF(dialogWidgetVar).curId
						&& PF(dialogWidgetVar).curId != cfg.pfdlgcid) {
					console.log("In nested dialog");
					dialogCloseButton.find('span.ui-icon').addClass(
							'ui-icon-back');
					dialogCloseButton.find('span.ui-icon').removeClass(
							'ui-icon-closethick');
				} else {
					dialogCloseButton.find('span.ui-icon').addClass(
							'ui-icon-closethick');
					dialogCloseButton.find('span.ui-icon').removeClass(
							'ui-icon-back');
				}

				var closeButton = $frame.contents().find('.close-button');
				dialogCloseButton.attr('title', closeButton.attr('title'));
				PF(dialogWidgetVar).closeButton = closeButton;
			} else {
				dialogCloseButton.hide();
			}

			var isOldIE = (navigator.userAgent.indexOf("MSIE") !== -1); // Detect
			// IE10
			// and
			// below

			if (typeof (iFrameResize) == "function") {
				iFrameResize(
						{
							heightCalculationMethod : isOldIE ? 'max'
									: 'lowestElement',
							maxHeight : window.innerHeight * 0.8,
							minHeight : window.innerHeight * 0.2,
							maxWidth : frameWidth,
							minWidth : frameWidth,
							scrolling : true,
							bodyMargin : "0 0 0.5em 0"
						}, dialogFrame[0]);
			}

			console.log("Showing dialog");
			PF(dialogWidgetVar).show();
			var doFocus = function() {
				$(dialogFrame).focus();
			};

			setTimeout(doFocus, 50);

			dialogFrame.data('initialized', true);
			doc.trigger('pfAjaxComplete');
		});
		dialogFrame.attr('src', frameURL);

		dialogCloseButton.click(function(e) {
			dialogCloseButton.prop('disabled', true);
			var widget = PF(dialogWidgetVar);
			if (dialogFrame.contents().find('.close-button')[0]) {
				console.log("Closing inner");
				dialogFrame.contents().find('.close-button').click();
			} else {
				console.log("Closing " + cfg.pfdlgcid);
				AgNO3DialogOverlay.closeDialogWidget(widget);
			}
			e.stopPropagation();
			e.preventDefault();
		});
	},

	closeDialog : function(cfg) {
		if (cfg && cfg.pfdlgcid) {
			var dlg = $(document.body).children('div.ui-dialog').filter(
					function() {
						return $(this).data('pfdlgcid') === cfg.pfdlgcid;
					}), dlgWidget = PF(dlg.data('widgetvar'));

			AgNO3DialogOverlay.closeDialogWidget(dlgWidget);
		} else {
			var dlg = $(document.body).children('div.ui-dialog')[0], dlgWidget = PF(dlg
					.data('widgetvar'));

			AgNO3DialogOverlay.closeDialogWidget(dlgWidget);
		}
	},

	closeDialogWidget : function(dlgWidget) {
		sourceWidget = dlgWidget.cfg.sourceWidget,
				sourceComponentId = dlgWidget.cfg.sourceComponentId,
				dialogReturnBehavior = null;

		console.log("Close dialog");

		if (sourceWidget && sourceWidget.cfg.behaviors) {
			dialogReturnBehavior = sourceWidget.cfg.behaviors['return'];
		} else if (sourceComponentId) {
			var dialogReturnBehaviorStr = $(
					document.getElementById(sourceComponentId)).data('return');
			if (dialogReturnBehaviorStr) {
				console.log("String behavior");
				dialogReturnBehavior = eval('(function(){'
						+ dialogReturnBehaviorStr + '})');
			}

		}

		if (dialogReturnBehavior) {
			var targetId = dlgWidget.jq.attr('data-pfdlgcid');
			var curId = dlgWidget.curId;
			console.log('Current ' + curId + ' target ' + targetId);

			var ext = {
				params : [ {
					name : sourceComponentId + '_pfdlgcid',
					value : targetId
				}, {
					name : sourceComponentId + '_curid',
					value : curId
				} ]
			};

			dialogReturnBehavior.call(this, ext);
		}

		console.log("Hide dialog");
		dlgWidget.hide();

		var doFocus = function() {
			if (document.activeElement) {
				document.activeElement.blur();
			}
		};

		setTimeout(doFocus, 50);
		console.log("Done");
	},
};

PrimeFaces.widget.DialogOpenHotkey = PrimeFaces.widget.BaseWidget.extend({
	init : function(cfg) {
		this._super(cfg);
		this.cfg = cfg;
		this.id = cfg.id;
		this.jqTarget = $(cfg.forTarget);
	},
});

PrimeFaces.widget.DialogOpenCommandLink = PrimeFaces.widget.BaseWidget.extend({
	init : function(cfg) {
		this._super(cfg);
		this.cfg = cfg;
		this.id = cfg.id;
		this.jqTarget = $(cfg.forTarget);
	},
});

PrimeFaces.widget.TreeTableFixed = PrimeFaces.widget.TreeTable
		.extend({

			sort : function(columnHeader, order) {
				var $this = this, options = {
					source : this.id,
					update : this.id,
					process : this.id,
					params : [ {
						name : this.id + '_sorting',
						value : true
					}, {
						name : this.id + '_sortKey',
						value : columnHeader.attr('id')
					}, {
						name : this.id + '_sortDir',
						value : order
					} ],
					onsuccess : function(responseXML, status, xhr) {
						PrimeFaces.ajax.Response
								.handle(
										responseXML,
										status,
										xhr,
										{
											widget : $this,
											handle : function(content) {
												this.tbody.html(content);

												columnHeader
														.siblings()
														.filter(
																'.ui-state-active')
														.removeData('sortorder')
														.removeClass(
																'ui-state-active')
														.find(
																'.ui-sortable-column-icon')
														.removeClass(
																'ui-icon-triangle-1-n ui-icon-triangle-1-s')
														.addClass(
																'ui-icon-carat-2-n-s');

												columnHeader
														.removeClass(
																'ui-state-hover')
														.addClass(
																'ui-state-active')
														.data('sortorder',
																order);
												var sortIcon = columnHeader
														.find('.ui-sortable-column-icon');

												if (order === 'DESCENDING')
													sortIcon
															.removeClass(
																	'ui-icon-triangle-1-n ui-icon-carat-2-n-s')
															.addClass(
																	'ui-icon-triangle-1-s');
												else if (order === 'ASCENDING')
													sortIcon
															.removeClass(
																	'ui-icon-triangle-1-s ui-icon-carat-2-n-s')
															.addClass(
																	'ui-icon-triangle-1-n');
											}
										});

						return true;
					},
					oncomplete : function(xhr, status, args) {
						if ($this.cfg.selectionMode && args.selection) {
							$this.selections = args.selection.split(',');
							$this.writeSelections();
						}
					}
				};

				if (this.hasBehavior('sort')) {
					var sortBehavior = this.cfg.behaviors['sort'];
					sortBehavior.call(this, options);
				} else {
					PrimeFaces.ajax.Request.handle(options);
				}
			},

			setupResizableColumns : function() {

				if (!this.cfg.liveResize) {
					this.resizerHelper = $(
							'<div class="ui-column-resizer-helper ui-state-highlight"></div>')
							.appendTo(this.jq);
				}

				this.thead
						.find('> tr > th.ui-resizable-column:not(:last-child)')
						.prepend(
								'<span class="ui-column-resizer">&nbsp;</span>');
				var resizers = this.thead
						.find('> tr > th > span.ui-column-resizer'), $this = this;

				resizers
						.draggable({
							axis : 'x',
							start : function() {
								if ($this.cfg.liveResize) {
									$this.jq.css('cursor', 'col-resize');
								} else {
									var height = $this.cfg.scrollable ? $this.scrollBody
											.height()
											: $this.thead.parent().height()
													- $this.thead.height() - 1;
									$this.resizerHelper.height(height);
									$this.resizerHelper.show();
								}
							},
							drag : function(event, ui) {
								if ($this.cfg.liveResize) {
									$this.resize(event, ui);
								} else {
									$this.resizerHelper.offset({
										left : ui.helper.offset().left
												+ ui.helper.width() / 2,
										top : $this.thead.offset().top
												+ $this.thead.height()
									});
								}
							},
							stop : function(event, ui) {
								var columnHeader = ui.helper.parent();
								ui.helper.css('left', '');

								if ($this.cfg.liveResize) {
									$this.jq.css('cursor', 'default');
								} else {
									$this.resize(event, ui);
									$this.resizerHelper.hide();
								}

								var nextColumnHeader = columnHeader.next(), tableWidth = columnHeader
										.closest('table').width(), change = null, newWidth = null, nextColumnWidth = null;

								var options = {
									source : $this.id,
									process : $this.id,
									params : [ {
										name : $this.id + '_colResize',
										value : true
									}, {
										name : $this.id + '_columnId',
										value : columnHeader.attr('id')
									}, {
										name : $this.id + '_width',
										value : columnHeader.outerWidth()
									}, {
										name : $this.id + '_height',
										value : columnHeader.height()
									}, {
										name : $this.id + '_nextColumnId',
										value : nextColumnHeader.attr('id')
									}, {
										name : $this.id + '_nextColumnWidth',
										value : nextColumnHeader.outerWidth()
									}, {
										name : $this.id + '_tableWidth',
										value : tableWidth
									}, ]
								}

								console.log(options);

								if ($this.hasBehavior('colResize')) {
									$this.cfg.behaviors['colResize'].call(
											$this, options);
								}
							},
							containment : this.jq
						});
			},

			resize : function(event, ui) {
				var columnHeader = ui.helper.parent(), change = null, newWidth = null, nextColumnWidth = null;

				if (this.cfg.liveResize) {
					change = columnHeader.outerWidth()
							- (event.pageX - columnHeader.offset().left),
							newWidth = (columnHeader.width() - change);
				} else {
					change = (ui.position.left - ui.originalPosition.left),
							newWidth = (columnHeader.width() + change);
				}

				newWidth = Math.round(newWidth);
				console.log("New width " + newWidth);

				if (newWidth > 50) {
					columnHeader.width(newWidth);
					var colIndex = columnHeader.index();
					columnHeader.closest("table").find("tbody tr")
							.each(
									function() {
										$(this).find('td').eq(colIndex).width(
												newWidth);
									});
					if (this.cfg.scrollable) {
						this.theadClone.find(
								PrimeFaces.escapeClientId(columnHeader
										.attr('id')
										+ '_clone')).width(newWidth);
						if (this.footerCols.length > 0) {
							var footerCol = this.footerCols.eq(colIndex);
							footerCol.width(newWidth);
						}
					}
				}
			},

			toggleCheckboxNode : function(node) {
				var selected = node.hasClass('ui-state-highlight'), rowKey = node
						.data('rk');

				// toggle itself
				if (selected)
					this.unselectNode(node, true);
				else
					this.selectNode(node, true);

				// propagate down
				if (this.cfg.propagateSelectionDown == "true"
						|| (!selected && this.cfg.propagateSelectionDown == "select")
						|| (selected && this.cfg.propagateSelectionDown == "deselect")) {
					var descendants = this.getDescendants(node);
					for (var i = 0; i < descendants.length; i++) {
						var descendant = descendants[i];
						if (selected)
							this.unselectNode(descendant, true);
						else
							this.selectNode(descendant, true);
					}

					if (selected) {
						this.removeDescendantsFromSelection(node.data('rk'));
					}
				}

				// propagate up
				if (this.cfg.propagateSelectionUp == "true"
						|| (!selected && this.cfg.propagateSelectionUp == "select")
						|| (selected && this.cfg.propagateSelectionUp == "deselect")) {
					var parentNode = this.getParent(node);
					if (parentNode) {
						this.propagateUp(parentNode);
					}
				}

				this.writeSelections();

				if (selected)
					this.fireUnselectNodeEvent(rowKey);
				else
					this.fireSelectNodeEvent(rowKey);
			},
		});

PrimeFaces.widget.DataTableFixed = PrimeFaces.widget.DataTable.extend({

	loadExpandedRowContent : function(row) {
		var $this = this, rowIndex = this.getRowMeta(row).index, rowKey = this
				.getRowMeta(row).key, options = {
			source : this.id,
			process : this.id,
			update : this.id,
			formId : this.cfg.formId,
			params : [ {
				name : this.id + '_rowExpansion',
				value : true
			}, {
				name : this.id + '_expandedRowIndex',
				value : rowIndex
			}, {
				name : this.id + '_expandedRowKey',
				value : rowKey
			}, {
				name : this.id + '_encodeFeature',
				value : true
			}, {
				name : this.id + '_skipChildren',
				value : true
			} ],
			onsuccess : function(responseXML, status, xhr) {
				PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
					widget : $this,
					handle : function(content) {
						if (content && $.trim(content).length) {
							row.addClass('ui-expanded-row');
							this.displayExpandedRow(row, content);
						}
					}
				});

				return true;
			},
			oncomplete : function() {
				$this.expansionProcess = $.grep($this.expansionProcess,
						function(r) {
							return r !== rowIndex;
						});
			}
		};

		if (this.hasBehavior('rowToggle')) {
			var rowToggleBehavior = this.cfg.behaviors['rowToggle'];

			rowToggleBehavior.call(this, options);
		} else {
			PrimeFaces.ajax.AjaxRequest(options);
		}
	},
});

PrimeFaces.widget.FileUploadFixed = PrimeFaces.widget.BaseWidget
		.extend({

			IMAGE_TYPES : /(\.|\/)(gif|jpe?g|png)$/i,

			init : function(cfg) {
				this._super(cfg);
				if (this.cfg.disabled) {
					return;
				}

				this.ucfg = {};
				this.form = this.jq.closest('form');
				this.buttonBar = this.jq.children('.ui-fileupload-buttonbar');
				this.chooseButton = this.buttonBar
						.children('.ui-fileupload-choose');
				this.uploadButton = this.buttonBar
						.children('.ui-fileupload-upload');
				this.cancelButton = this.buttonBar
						.children('.ui-fileupload-cancel');
				this.content = this.jq.children('.ui-fileupload-content');
				this.filesTbody = this.content
						.find('> div.ui-fileupload-files > div');
				this.sizes = [ 'Bytes', 'KB', 'MB', 'GB', 'TB' ];
				this.files = [];
				this.fileAddIndex = 0;
				this.cfg.invalidFileMessage = this.cfg.invalidFileMessage
						|| 'Invalid file type';
				this.cfg.invalidSizeMessage = this.cfg.invalidSizeMessage
						|| 'Invalid file size';
				this.cfg.fileLimitMessage = this.cfg.fileLimitMessage
						|| 'Maximum number of files exceeded';
				this.cfg.messageTemplate = this.cfg.messageTemplate
						|| '{name} {size}';
				this.cfg.previewWidth = this.cfg.previewWidth || 80;
				this.uploadedFileCount = 0;

				this.renderMessages();

				this.bindEvents();

				var $this = this, postURL = this.form.attr('action'), encodedURLfield = this.form
						.children("input[name*='javax.faces.encodedURL']");

				// portlet support
				var porletFormsSelector = null;
				if (encodedURLfield.length > 0) {
					porletFormsSelector = 'form[action="' + postURL + '"]';
					postURL = encodedURLfield.val();
				}

				this.ucfg = {
					url : postURL,
					portletForms : porletFormsSelector,
					paramName : this.id,
					dataType : 'xml',
					dropZone : (this.cfg.dnd === false) ? null : this.jq,
					sequentialUploads : this.cfg.sequentialUploads,
					formData : function() {
						return $this.createPostData();
					},
					beforeSend : function(xhr, settings) {
						xhr.setRequestHeader('Faces-Request', 'partial/ajax');
						xhr.pfSettings = settings;
						xhr.pfArgs = {}; // default should be an empty object
					},
					start : function(e) {
						if ($this.cfg.onstart) {
							$this.cfg.onstart.call($this);
						}
					},
					add : function(e, data) {
						$this.chooseButton
								.removeClass('ui-state-hover ui-state-focus');

						if ($this.fileAddIndex === 0) {
							$this.clearMessages();
						}

						if ($this.cfg.fileLimit
								&& ($this.uploadedFileCount
										+ $this.files.length + 1) > $this.cfg.fileLimit) {
							$this.clearMessages();
							$this.showMessage({
								summary : $this.cfg.fileLimitMessage
							});

							return;
						}

						var file = data.files ? data.files[0] : null;
						if (file) {
							var validMsg = $this.validate(file);

							if (validMsg) {
								$this.showMessage({
									summary : validMsg,
									filename : PrimeFaces.escapeHTML(file.name),
									filesize : file.size
								});
							} else {
								var row = $('<div class="ui-fileupload-row"></div>');

								if ($this.cfg.enablePreview) {
									row
											.append('<div class="ui-fileupload-preview"></div>');
								}

								row
										.append('<div>' + PrimeFaces.escapeHTML(file.name) + '</td>')
										.append(
												'<div>'
														+ $this
																.formatSize(file.size)
														+ '</div>')
										.append(
												'<div class="ui-fileupload-progress"></div>')
										.append(
												'<div><button class="ui-fileupload-cancel ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"><span class="ui-button-icon-left ui-icon ui-icon ui-icon-close"></span><span class="ui-button-text">ui-button</span></button></div>')
										.appendTo($this.filesTbody);
								
								 if($this.filesTbody.children('.ui-fileupload-row').length > 1) {
									 $('<div class="ui-widget-content"></div>').prependTo(row);
								 }

								// preview
								if ($this.cfg.enablePreview
										&& $this.isCanvasSupported()
										&& window.File && window.FileReader
										&& $this.IMAGE_TYPES.test(file.name)) {
									var imageCanvas = $('<canvas></canvas>')
											.appendTo(
													row
															.children('div.ui-fileupload-preview')), context = imageCanvas
											.get(0).getContext('2d'), winURL = window.URL
											|| window.webkitURL, url = winURL
											.createObjectURL(file), img = new Image();

									img.onload = function() {
										var imgWidth = null, imgHeight = null, scale = 1;

										if ($this.cfg.previewWidth > this.width) {
											imgWidth = this.width;
										} else {
											imgWidth = $this.cfg.previewWidth;
											scale = $this.cfg.previewWidth
													/ this.width;
										}

										var imgHeight = parseInt(this.height
												* scale);

										imageCanvas.attr({
											width : imgWidth,
											height : imgHeight
										});
										context.drawImage(img, 0, 0, imgWidth,
												imgHeight);
									}

									img.src = url;
								}

								// progress
								row
										.children('div.ui-fileupload-progress')
										.append(
												'<div class="ui-progressbar ui-widget ui-widget-content ui-corner-all" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="ui-progressbar-value ui-widget-header ui-corner-left" style="display: none; width: 0%;"></div></div>');

								file.row = row;

								file.row.data('filedata', data);

								$this.files.push(file);

								if ($this.cfg.auto) {
									$this.upload();
								}
							}

							if ($this.files.length > 0) {
								$this.enableButton($this.uploadButton);
								$this.enableButton($this.cancelButton);
							}

							$this.fileAddIndex++;
							if ($this.fileAddIndex === (data.originalFiles.length)) {
								$this.fileAddIndex = 0;
							}
						}
					},
					send : function(e, data) {
						if (!window.FormData) {
							for (var i = 0; i < data.files.length; i++) {
								var file = data.files[i];

								file.row
										.children('.ui-fileupload-progress')
										.find(
												'> .ui-progressbar > .ui-progressbar-value')
										.addClass('ui-progressbar-value-legacy')
										.css({
											width : '100%',
											display : 'block'
										});
							}
						}
					},
					fail : function(e, data) {
						if ($this.cfg.onerror) {
							$this.cfg.onerror.call($this);
						}
					},
					progress : function(e, data) {
						if (window.FormData) {
							var progress = parseInt(data.loaded / data.total
									* 100, 10);

							for (var i = 0; i < data.files.length; i++) {
								var file = data.files[i];

								file.row
										.children('.ui-fileupload-progress')
										.find(
												'> .ui-progressbar > .ui-progressbar-value')
										.css({
											width : progress + '%',
											display : 'block'
										});
							}
						}
					},
					done : function(e, data) {
						$this.uploadedFileCount += data.files.length;
						$this.removeFiles(data.files);

						PrimeFaces.ajax.Response.handle(data.result,
								data.textStatus, data.jqXHR, null);
					},
					always : function(e, data) {
						if ($this.cfg.oncomplete) {
							$this.cfg.oncomplete.call($this, data.jqXHR.pfArgs);
						}
					}
				};

				this.jq.fileupload(this.ucfg);
			},

			bindEvents : function() {
				var $this = this;

				PrimeFaces.skinButton(this.buttonBar.children('button'));

				this.chooseButton.on('mouseover.fileupload', function() {
					var el = $(this);
					if (!el.prop('disabled')) {
						el.addClass('ui-state-hover');
					}
				}).on('mouseout.fileupload', function() {
					$(this).removeClass('ui-state-active ui-state-hover');
				}).on(
						'mousedown.fileupload',
						function() {
							var el = $(this);
							if (!el.prop('disabled')) {
								el.addClass('ui-state-active').removeClass(
										'ui-state-hover');
							}
						}).on(
						'mouseup.fileupload',
						function() {
							$(this).removeClass('ui-state-active').addClass(
									'ui-state-hover');
						});

				var isChooseButtonClick = false;
				this.chooseButton.on('focus.fileupload', function() {
					$(this).addClass('ui-state-focus');
				}).on('blur.fileupload', function() {
					$(this).removeClass('ui-state-focus');
					isChooseButtonClick = false;
				});

				// For JAWS support
				this.chooseButton.on('click.fileupload', function() {
					$this.chooseButton.children('input').trigger('click');
				}).on(
						'keydown.fileupload',
						function(e) {
							var keyCode = $.ui.keyCode, key = e.which;

							if (key === keyCode.SPACE || key === keyCode.ENTER
									|| key === keyCode.NUMPAD_ENTER) {
								$this.chooseButton.children('input').trigger(
										'click');
								$(this).blur();
								e.preventDefault();
							}
						});

				this.chooseButton.children('input').on('click', function(e) {
					if (isChooseButtonClick) {
						isChooseButtonClick = false;
						e.preventDefault();
						e.stopPropagation();
					} else {
						isChooseButtonClick = true;
					}
				});

				this.uploadButton.on('click.fileupload', function(e) {
					$this.disableButton($this.uploadButton);
					$this.disableButton($this.cancelButton);
					$this.disableButton($this.filesTbody.find(
							'> div > div:last-child').children(
							'.ui-fileupload-cancel'));

					$this.upload();

					e.preventDefault();
				});

				this.cancelButton.on('click.fileupload', function(e) {
					$this.clear();
					$this.disableButton($this.uploadButton);
					$this.disableButton($this.cancelButton);

					e.preventDefault();
				});

				this.clearMessageLink.on('click.fileupload', function(e) {
					$this.messageContainer.fadeOut(function() {
						$this.messageList.children().remove();
					});

					e.preventDefault();
				});

				this.rowActionSelector = this.jqId
						+ " .ui-fileupload-files button";
				this.rowCancelActionSelector = this.jqId
						+ " .ui-fileupload-files .ui-fileupload-cancel";
				this.clearMessagesSelector = this.jqId
						+ " .ui-messages .ui-messages-close";

				$(document)
						.off(
								'mouseover.fileupload mouseout.fileupload mousedown.fileupload mouseup.fileupload focus.fileupload blur.fileupload click.fileupload ',
								this.rowCancelActionSelector)
						.on('mouseover.fileupload',
								this.rowCancelActionSelector, null,
								function(e) {
									$(this).addClass('ui-state-hover');
								})
						.on(
								'mouseout.fileupload',
								this.rowCancelActionSelector,
								null,
								function(e) {
									$(this).removeClass(
											'ui-state-hover ui-state-active');
								})
						.on(
								'mousedown.fileupload',
								this.rowCancelActionSelector,
								null,
								function(e) {
									$(this).addClass('ui-state-active')
											.removeClass('ui-state-hover');
								})
						.on(
								'mouseup.fileupload',
								this.rowCancelActionSelector,
								null,
								function(e) {
									$(this).addClass('ui-state-hover')
											.removeClass('ui-state-active');
								})
						.on('focus.fileupload', this.rowCancelActionSelector,
								null, function(e) {
									$(this).addClass('ui-state-focus');
								})
						.on('blur.fileupload', this.rowCancelActionSelector,
								null, function(e) {
									$(this).removeClass('ui-state-focus');
								})
						.on(
								'click.fileupload',
								this.rowCancelActionSelector,
								null,
								function(e) {
									var row = $(this).closest('.ui-fileupload-row'), removedFile = $this.files
											.splice(row.index(), 1);
									removedFile[0].row = null;

									$this.removeFileRow(row);

									if ($this.files.length === 0) {
										$this.disableButton($this.uploadButton);
										$this.disableButton($this.cancelButton);
									}

									e.preventDefault();
								});
			},

			upload : function() {
				for (var i = 0; i < this.files.length; i++) {
					this.files[i].row.data('filedata').submit();
				}
			},

			createPostData : function() {
				var process = this.cfg.process ? this.id
						+ ' '
						+ PrimeFaces.expressions.SearchExpressionFacade
								.resolveComponents(this.cfg.process).join(' ')
						: this.id;
				var params = this.form.serializeArray();

				params.push({
					name : PrimeFaces.PARTIAL_REQUEST_PARAM,
					value : 'true'
				});
				params.push({
					name : PrimeFaces.PARTIAL_PROCESS_PARAM,
					value : process
				});
				params.push({
					name : PrimeFaces.PARTIAL_SOURCE_PARAM,
					value : this.id
				});

				if (this.cfg.update) {
					var update = PrimeFaces.expressions.SearchExpressionFacade
							.resolveComponents(this.cfg.update).join(' ');
					params.push({
						name : PrimeFaces.PARTIAL_UPDATE_PARAM,
						value : update
					});
				}

				return params;
			},

			formatSize : function(bytes) {
				if (bytes === undefined)
					return '';

				if (bytes === 0)
					return 'N/A';

				var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
				if (i === 0)
					return bytes + ' ' + this.sizes[i];
				else
					return (bytes / Math.pow(1024, i)).toFixed(1) + ' '
							+ this.sizes[i];
			},

			removeFiles : function(files) {
				for (var i = 0; i < files.length; i++) {
					this.removeFile(files[i]);
				}
			},

			removeFile : function(file) {
				var $this = this;

				this.files = $
						.grep(
								this.files,
								function(value) {
									return (value.name === file.name && value.size === file.size);
								}, true);

				$this.removeFileRow(file.row);
				file.row = null;
			},

			removeFileRow : function(row) {
				row.fadeOut(function() {
					$(this).remove();
				});
			},

			clear : function() {
				for (var i = 0; i < this.files.length; i++) {
					this.removeFileRow(this.files[i].row);
					this.files[i].row = null;
				}

				this.clearMessages();

				this.files = [];
			},

			validate : function(file) {
				if (this.cfg.allowTypes
						&& !(this.cfg.allowTypes.test(file.type) || this.cfg.allowTypes
								.test(file.name))) {
					return this.cfg.invalidFileMessage;
				}

				if (this.cfg.maxFileSize && file.size > this.cfg.maxFileSize) {
					return this.cfg.invalidSizeMessage;
				}

				return null;
			},

			renderMessages : function() {
				var markup = '<div class="ui-messages ui-widget ui-helper-hidden"><div class="ui-messages-error ui-corner-all">'
						+ '<a class="ui-messages-close" href="#"><span class="ui-icon ui-icon-close"></span></a>'
						+ '<span class="ui-messages-error-icon"></span>'
						+ '<ul></ul>' + '</div></div>';

				this.messageContainer = $(markup).prependTo(this.content);
				this.messageList = this.messageContainer
						.find('> .ui-messages-error > ul');
				this.clearMessageLink = this.messageContainer
						.find('> .ui-messages-error > a.ui-messages-close');
			},

			clearMessages : function() {
				this.messageContainer.hide();
				this.messageList.children().remove();
			},

			showMessage : function(msg) {
				var summary = msg.summary, detail = '';

				if (msg.filename && msg.filesize) {
					detail = this.cfg.messageTemplate.replace('{name}',
							msg.filename).replace('{size}',
							this.formatSize(msg.filesize));
				}

				this.messageList
						.append('<li><span class="ui-messages-error-summary">'
								+ summary
								+ '</span><span class="ui-messages-error-detail">'
								+ detail + '</span></li>');
				this.messageContainer.show();
			},

			disableButton : function(btn) {
				btn
						.prop('disabled', true)
						.addClass('ui-state-disabled')
						.removeClass(
								'ui-state-hover ui-state-active ui-state-focus');
			},

			enableButton : function(btn) {
				btn.prop('disabled', false).removeClass('ui-state-disabled');
			},

			isCanvasSupported : function() {
				var elem = document.createElement('canvas');
				return !!(elem.getContext && elem.getContext('2d'));
			}

		});

PrimeFaces.widget.ExtendedAjaxStatus = PrimeFaces.widget.AjaxStatus
		.extend({

			init : function(cfg) {
				this._super(cfg);
				$this = this;
				$this.delay = {};
				$(document)
						.on(
								'pfAjaxDelay',
								function() {
									$this.delay['complete'] = 'complete' in $this.delay ? $this.delay['complete'] + 1
											: 1;
								});

				$(document)
						.on(
								'pfAjaxStart',
								function() {
									$this.delay['complete'] = 'complete' in $this.delay ? $this.delay['complete'] + 1
											: 1;
								});
			},

			trigger : function(event, args) {
				if (event in this.delay && this.delay[event] > 0) {
					this.delay[event] = this.delay[event] - 1;
					if (this.delay[event] > 0) {
						console.log("Ignoring delayed " + event + ": "
								+ this.delay[event]);
						return;
					}
				} else if (event == 'error') {
					this.delay = {};
				}

				var callback = this.cfg[event];
				if (callback) {
					callback.apply(document, args);
				}

				this.jq.children().hide().filter(this.jqId + '_' + event)
						.show();
			},
		});

PrimeFaces.widget.WizardFixed = PrimeFaces.widget.Wizard.extend({
	

	loadStep : function(stepToGo, isBack) {
		console.log("loadStep");
		var $this = this, options = {
			source : this.id,
			process : this.id + (this.cfg.process ? " " + this.cfg.process : ""),
			update : this.id + (this.cfg.update ? " " + this.cfg.update : ""),
			formId : this.cfg.formId,
			params : [ {
				name : this.id + '_wizardRequest',
				value : true
			}, {
				name : this.id + '_stepToGo',
				value : stepToGo
			} ],
			onstart : function() {
				if ( $this.cfg.onstart ) {
					$this.cfg.onstart();
				}
			},
			onerror : function(xhr, status, exception) {
				if ( $this.cfg.onerror ) {
					$this.cfg.onerror(xhr, status, exception);
				}
			},
			onsuccess : function(responseXML, status, xhr) {
				PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
					widget : $this,
					handle : function(content) {
						this.content.html(content);
					}
				});
				
				if ( $this.cfg.onsuccess ) {
					$this.cfg.onsuccess(responseXML, status, xhr);
				}

				return true;
			},
			oncomplete : function(xhr, status, args) {
				$this.currentStep = args.currentStep;

				if (!args.validationFailed) {
					var currentStepIndex = $this
							.getStepIndex($this.currentStep);

					if ($this.cfg.showNavBar) {
						if (currentStepIndex === $this.cfg.steps.length - 1) {
							$this.hideNextNav();
							$this.showBackNav();
						} else if (currentStepIndex === 0) {
							$this.hideBackNav();
							$this.showNextNav();
						} else {
							$this.showBackNav();
							$this.showNextNav();
						}
					}

					// update step status
					if ($this.cfg.showStepStatus) {
						$this.stepControls.removeClass('ui-state-highlight');
						$($this.stepControls.get(currentStepIndex)).addClass(
								'ui-state-highlight');
					}
				}
				
				if ( $this.cfg.oncomplete ) {
					$this.cfg.oncomplete(xhr, status, args);
				}
			}
		};

		if (isBack) {
			options.params.push({
				name : this.id + '_backRequest',
				value : true
			});
		}

		PrimeFaces.ajax.Request.handle(options);
	},
});

/*
 * IE8 Polyfils for iframeResizer.js
 * 
 * Public domain code - Mozilla Contributors https://developer.mozilla.org/
 */
if (!Array.prototype.forEach) {
	Array.prototype.forEach = function(fun /* , thisArg */) {
		"use strict";
		if (this === void 0 || this === null || typeof fun !== "function")
			throw new TypeError();

		var t = Object(this), len = t.length >>> 0, thisArg = arguments.length >= 2 ? arguments[1]
				: void 0;

		for (var i = 0; i < len; i++)
			if (i in t)
				fun.call(thisArg, t[i], i, t);
	};
}

if (!Function.prototype.bind) {
	Function.prototype.bind = function(oThis) {
		if (typeof this !== 'function') {
			// closest thing possible to the ECMAScript 5
			// internal IsCallable function
			throw new TypeError(
					'Function.prototype.bind - what is trying to be bound is not callable');
		}

		var aArgs = Array.prototype.slice.call(arguments, 1), fToBind = this, fNOP = function() {
		}, fBound = function() {
			return fToBind.apply(this instanceof fNOP ? this : oThis, aArgs
					.concat(Array.prototype.slice.call(arguments)));
		};

		fNOP.prototype = this.prototype;
		fBound.prototype = new fNOP();

		return fBound;
	};
}

if (!Array.prototype.forEach) {
	Array.prototype.forEach = function(callback, thisArg) {
		if (this === null)
			throw new TypeError(' this is null or not defined');
		if (typeof callback !== 'function')
			throw new TypeError(callback + ' is not a function');

		var O = Object(this), len = O.length >>> 0;

		for (var k = 0; k < len; k++) {
			if (k in O)
				callback.call(thisArg, O[k], k, O);
		}
	};
}

if (!Element.prototype.matches) {
	Element.prototype.matches = Element.prototype.matchesSelector
			|| Element.prototype.mozMatchesSelector
			|| Element.prototype.msMatchesSelector
			|| Element.prototype.oMatchesSelector
			|| Element.prototype.webkitMatchesSelector
			|| function(s) {
				var matches = (this.document || this.ownerDocument)
						.querySelectorAll(s), i = matches.length;
				while (--i >= 0 && matches.item(i) !== this) {
				}
				return i > -1;
			};
}
