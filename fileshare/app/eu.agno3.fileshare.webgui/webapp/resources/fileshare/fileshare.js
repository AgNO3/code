if (!window.console)
	console = {
		log : function() {
		}
	};

AgNO3FileUpload = {

	actionMappings : {
		'i' : 'info-action',
		's' : 'share-action',
		'd' : 'download-action',
		'u' : 'upload-action',
		'v' : 'view-action',
		'm' : 'move-action',
		'n' : 'rename-action',
		'c' : 'create-dir-action',
		'f' : 'favorite-action',
		'x' : 'clear-selection-action'
	},

	init : function(widget, onUploadError) {
		var widgetId = widget.attr('id');
		var encWidgetId = "#" + widgetId.replace(/:/g, '\\:');
		var uploadWidget = widget.find(".file-upload");
		var pfFileTableWidget = widgetFromJq(widget.find(".file-table"));
		var confirmDialogWidget = PF('confirmReplaceDialog');

		$(document).bind('drop dragover', function(e) {
			e.preventDefault();
		});

		var addHandler = AgNO3FileUpload.makeMainViewAddHandler(widget);
		var pfUploadWidget = AgNO3FileUpload.initUploadDialog(uploadWidget,
				widget, onUploadError, addHandler, function() {
					AgNO3FileUpload.setupAfterUpdate(widget, addHandler);
				});

		$(encWidgetId).on('dragover', '.drop-target', function(e) {
			var container = $(e.target).closest("div.drop-target");
			if (!container) {
				return;
			}

			if (pfUploadWidget && e.originalEvent.dataTransfer) {
				$('.file-upload').data().blueimpFileupload._onDragOver(e);
			}

			e.stopPropagation();
			e.preventDefault();
			AgNO3FileUpload.onDragOver(e);
		});

		$(encWidgetId).on('dragleave', '.drop-target',
				AgNO3FileUpload.onDragLeave);

		$(encWidgetId).on('drop', '.drop-target', function(e) {
			var container = $(e.target).closest("div.drop-target");

			if (container && e.originalEvent.dataTransfer) {
				if (pfUploadWidget) {
					$('.file-upload').data().blueimpFileupload._onDrop(e);
				}
				e.stopPropagation();
				e.preventDefault();
				AgNO3FileUpload.onDrop(e);
			}
		});

		$(document).keydown(
				function(e) {
					// hack: prevent scrolling using space, page up, page down,
					// pos1, end
					if (e.keyCode >= 32 && e.keyCode <= 36
							&& e.target == document.body) {
						// e.preventDefault();
					}
				});

		$("body").on('keypress', function(e) {
			AgNO3FileUpload.onKeyPress(widget, pfFileTableWidget, e);
		});

		$(encWidgetId).on(
				'draghold',
				'.file-table .ui-treetable-data div.drop-target',
				function(e) {
					var container = $(e.target);
					var row = container.closest('tr');
					if (widget.data('single-level')) {
						return;
					}
					if (!row.attr('aria-expanded')
							|| row.attr('aria-expanded') == "false") {
						var icon = container.find("span.ui-icon");
						row.attr('aria-expanded', 'true');
						pfFileTableWidget.expandNode(row);
						icon.removeClass("ui-icon-folder-collapsed");
						icon.addClass("ui-icon-folder-open");
					}
				});
		
		$(encWidgetId).on("click", ".file-table .ui-treetable-data .share-action .grant-list", function(e) {
			$('.selection-operations .share-action').click();
		});

		$(encWidgetId)
				.on(
						"click",
						'.file-table .ui-treetable-data div.drop-target, .file-table .ui-treetable-data tr.ui-treetable-selectable-node',
						function(e) {
							e.target = $(this).closest('td');
							var node = $(this).closest('tr');

							if (!node.is('.ui-treetable-selectable-node')) {
								return;
							}
							var nodeKey = node.attr('data-rk');
							var meta = e.metaKey || e.ctrlKey;

							if (pfFileTableWidget.isMultipleSelection()
									&& e.shiftKey) {
								if (meta && pfFileTableWidget.cursorNode) {
									// fix multi selection behaviour
									var currentNodeIndex = node.index(), cursorNodeIndex = pfFileTableWidget.cursorNode
											.index(), startIndex = (currentNodeIndex > cursorNodeIndex) ? cursorNodeIndex
											: currentNodeIndex, endIndex = (currentNodeIndex > cursorNodeIndex) ? (currentNodeIndex + 1)
											: (cursorNodeIndex + 1), nodes = pfFileTableWidget.tbody
											.children();

									for (var i = startIndex; i < endIndex; i++) {
										pfFileTableWidget.selectNode(nodes
												.eq(i), true);
									}
								} else {
									pfFileTableWidget.selectNodesInRange(node);
								}
								// fix missing select event for range selection
								pfFileTableWidget.fireSelectNodeEvent(nodeKey);
								return;
							}
							pfFileTableWidget.onRowClick(e, node);

							if (meta) {
								pfFileTableWidget.cursorNode = node;
							}
						});

		$(encWidgetId)
				.on(
						"click",
						'.file-table a.file-name',
						function(e) {
							var entity = $(this).closest("a.file-name");
							var container = $(this).closest("div");
							var icon = entity.find("span.ui-icon");
							var row = $(this).closest('tr');

							if (e.ctrlKey) {
								e.stopPropagation();
								return;
							}

							if (container.attr('data-collabsible') == "true") {
								if (row.attr('aria-expanded') == "true") {
									row.attr('aria-expanded', 'false');
									row.attr('aria-expanding', false);
									pfFileTableWidget.collapseNode(row);
									icon.removeClass(container
											.attr('data-expanded-icon'));
									icon.addClass(container
											.attr('data-collapsed-icon'));
								} else if (row.attr('aria-expanding') == 'true') {
									console
											&& console.log
											&& console
													.log("Currently expanding");
								} else {
									row.attr('aria-expanding', 'true');
									pfFileTableWidget.expandNode(row);
									icon.removeClass(container
											.attr('data-collapsed-icon'));
									icon.addClass(container
											.attr('data-expanded-icon'));
								}
							}
						});

		$(encWidgetId).on(
				"dblclick",
				'.file-table a.file-name',
				function() {
					var entity = $(this).closest("a.file-name");
					var container = $(this).closest("div");
					var icon = entity.find("span.ui-icon");
					var row = $(this).closest('tr');

					if (container.data("type") == "file"
							|| container.data("type") == "share-root-file") {
						row.find('.action-column .download-link').click();
					}
				});

		$(encWidgetId)
				.on(
						'mouseenter dragenter',
						'.file-table .ui-treetable-data div.drop-target, .file-table .ui-treetable-data div.drop-target *',
						function() {
							var elem = $(this).closest('div.drop-target');
							clearTimeout(elem.data('timeoutId'));
							if (elem.data('hover')) {
								return;
							}
							elem.data('hover', true);

						})

		$(encWidgetId).on('mouseleave dragleave',
				'.file-table .ui-treetable-data div.drop-target', function() {
					var elem = $(this), timeoutId = setTimeout(function() {
						elem.data('hover', false);
					}, 100);
					elem.data('timeoutId', timeoutId);
				});

		if (pfUploadWidget) {
			$(encWidgetId)
					.on(
							'fileuploaddrop',
							function(e) {
								var target = e.delegatedEvent.originalEvent.originalTarget
										|| e.delegatedEvent.originalEvent.target
										|| e.originalEvent.originalTarget;

								var container = $(target).closest(
										"div.drop-target");
								var tr = $(target).closest("tr");

								var id, grantId, label;

								if (container.is('.file-table')
										&& container.closest('.file-browser')
												.data('single-level')) {
									// drop in between table elements
									console.log("SL "
											+ container
													.closest('.file-browser')
													.data('single-level'));
									console
											&& console.log
											&& console
													.log("Dropped between elements");
									container = container
											.closest('.file-browser');
									id = container.attr('data-root-id');
									grantId = container.attr('data-grant-id');
									label = container.attr('data-root-label');
								} else if (!container || !tr
										|| !container.attr('data-id')) {
									console.log("not valid");
									console.log(container);
									return;
								} else {
									id = container.attr('data-id');
									grantId = container.attr('data-grant-id');
									label = container.attr('data-label');
								}

								var filesList = e.delegatedEvent.dataTransfer.files
										|| e.dataTransfer.files;

								for (var i = 0, len = filesList.length; i < len; i++) {
									filesList[i].numFiles = filesList.length;
								}

								$(encWidgetId + ' .file-upload')
										.fileupload(
												'add',
												{
													files : filesList,
													uploadTemplate : pfUploadWidget.cfg.uploadTemplate,
													downloadTemplate : pfUploadWidget.cfg.downloadTemplate,
													sequentialUploads : true,
													singleFileUploads : true,
													limitConcurrentUploads : 1,
													fromDragDrop : true,
													customData : {
														upload_target : id,
														upload_targetGrant : grantId,
														upload_targetLabel : label,
														upload_targetRow : container
													}
												})
								e.preventDefault();
							});
		}

		AgNO3FileUpload.setupAfterUpdate(widget, addHandler);

		PF('uiBlocker') && PF('uiBlocker').hide();
	},

	initFileReplaceUpload : function(widget) {
		var outer = widget.closest('form');
		var addHandler = function(pfUploadWidget, e, data) {
			if (!data) {
				console.log("No data");
				return;
			}

			var files = data.files;
			if (files.length > 1 || files[0].size === 0) {
				console.log("Looks like a folder");
				alert($('.message-no-folder-support').text());
				return;
			}

			var uploadDialog = PF('fileUploadDialog');
			var targetId = uploadDialog.jq.find(
					'input[name=upload_target_file]').val();
			if (!targetId) {
				console.log("Missing target");
				return;
			}
			var grantId = uploadDialog.jq.find(
					'input[name=upload_target_grant]').val();
			var label = uploadDialog.jq.find('input[name=upload_target_label]')
					.val();
			AgNO3FileUpload.doAddFile(pfUploadWidget, outer, e, data, targetId,
					grantId, label, true, true, true);
		};
		var pfUploadWidget = AgNO3FileUpload.initUploadDialog(widget, outer,
				function() {
				}, addHandler, function(uploadWidget, widget, addHandler) {
					AgNO3FileUpload.setupDialogAfterUpdate(uploadWidget,
							widget, addHandler);
				});
		AgNO3FileUpload.setupDialogAfterUpdate(widget, outer, addHandler);
	},

	onKeyPress : function(widget, pfFileTableWidget, e) {
		if (e.target.tagName.toLowerCase() == "input"
				|| e.target.tagName.toLowerCase() == "textarea") {
			if (e.keyCode == 13) {
				// prevent enter from submitting form
				e.preventDefault();
			}
			e.stopPropagation();
			return;
		}

		if (!e.ctrlKey && (e.keyCode == 40 || e.keyCode == 38)) {
			if (pfFileTableWidget.selections.length > 0) {

				if (e.keyCode == 38) {
					var selected = pfFileTableWidget.jq
							.find(".ui-treetable-data > tr[data-rk="
									+ pfFileTableWidget.selections[0] + "]");
				} else if (e.keyCode == 40) {
					// downward
					var selected = pfFileTableWidget.jq
							.find(".ui-treetable-data > tr[data-rk="
									+ pfFileTableWidget.selections[pfFileTableWidget.selections.length - 1]
									+ "]");
				}

				if (e.keyCode == 40) {
					var toSelect = selected.next();
					if (!e.shiftKey
							&& (!toSelect || typeof toSelect.data('rk') == 'undefined')) {
						toSelect = pfFileTableWidget.jq.find(
								".ui-treetable-data > tr").first();
					}
				} else {
					var toSelect = selected.prev();
					if (!e.shiftKey
							&& (!toSelect || typeof toSelect.data('rk') == 'undefined')) {
						toSelect = pfFileTableWidget.jq.find(
								".ui-treetable-data > tr").last();
					}
				}

				if (!toSelect || !toSelect.is('.ui-treetable-selectable-node')) {
					return;
				}

				e.target = toSelect.find("td").first();
				pfFileTableWidget.onRowClick(e, toSelect);

				// fire node select event for multi
				// selection change
				var nodeKey = toSelect.attr('id').split('_node_')[1];
				if (e.shiftKey) {
					pfFileTableWidget.fireSelectNodeEvent(nodeKey);
				}
			} else {
				if (e.keyCode == 40) {
					var node = pfFileTableWidget.jq.find(
							".ui-treetable-data > tr").first();
				} else {
					var node = pfFileTableWidget.jq.find(
							".ui-treetable-data > tr").last();
				}

				if (!node.is('.ui-treetable-selectable-node')) {
					return;
				}

				e.target = node.find("td").first();
				pfFileTableWidget.onRowClick(e, node);
			}
		} else if (!widget.data('single-level')
				&& (e.keyCode == 37 || e.keyCode == 39)
				&& pfFileTableWidget.selections.length == 1) {
			// right or left

			var row = pfFileTableWidget.jq
					.find(".ui-treetable-data > tr[data-rk="
							+ pfFileTableWidget.selections[0] + "]");
			var entity = $(row).find("a.file-name");
			var container = $(entity).closest("div");
			var icon = entity.find("span.ui-icon");

			if (container.attr('data-collabsible') == "true") {
				if (row.attr('aria-expanded') == "true" && e.keyCode == 37) {
					row.attr('aria-expanded', 'false');
					row.attr('aria-expanding', false);
					pfFileTableWidget.collapseNode(row);
					icon.removeClass(container.attr('data-expanded-icon'));
					icon.addClass(container.attr('data-collapsed-icon'));
				} else if (row.attr('aria-expanding') == 'true') {
				} else if (e.keyCode == 39) {
					row.attr('aria-expanding', 'true');
					pfFileTableWidget.expandNode(row);
					icon.removeClass(container.attr('data-collapsed-icon'));
					icon.addClass(container.attr('data-expanded-icon'));
				}
			}
		} else if (widget.data('single-level')
				&& (e.keyCode == 37 || e.keyCode == 39)) {
			if (e.keyCode == 39 && pfFileTableWidget.selections.length == 1) {
				// go into
				var row = pfFileTableWidget.jq
						.find(".ui-treetable-data > tr[data-rk="
								+ pfFileTableWidget.selections[0] + "]");
				var link = $(row).find("td a.single-level-file-name");
				var entity = $(link).closest("div");
				if (entity.data('type') != "file") {
					link[0].click();
				}
			} else if (e.keyCode == 37) {
				// go up
				var pathCrumbs = pfFileTableWidget.jq.closest('.file-browser')
						.find(".file-toolbar .ui-breadcrumb.root-display");

				var items = pathCrumbs
						.find("li[role=menuitem] a.ui-menuitem-link");
				var prevLink = items[items.length - 2];
				if (prevLink) {
					prevLink.click();
				}
			}

		} else if (e.keyCode >= 37 && e.keyCode <= 40) {
			// ignore
		} else if (e.key
				&& e.key.toLowerCase() in AgNO3FileUpload.actionMappings) {

			var mappedTo = AgNO3FileUpload.actionMappings[e.key.toLowerCase()];

			var action;
			if (pfFileTableWidget.selections.length == 1) {
				var row = pfFileTableWidget.jq
						.find(".ui-treetable-data > tr[data-rk="
								+ pfFileTableWidget.selections[0] + "]");
				action = row.find(mappedTo);
			}

			var toolbar;

			if (pfFileTableWidget.selections.length == 0) {
				toolbar = pfFileTableWidget.jq.closest('.file-browser').find(
						".file-toolbar .container-operations");
			} else {
				toolbar = pfFileTableWidget.jq.closest('.file-browser').find(
						".file-toolbar .selection-operations");
			}

			if (action && action[0]) {
				action[0].click();
			} else {
				action = toolbar.find(".action-link." + mappedTo);
				if (action[0]) {
					action[0].click();
				}
			}
		} else {
			return;
		}
		e.stopPropagation();
		e.preventDefault();
	},

	initUploadDialog : function(uploadWidget, widget, onUploadError,
			addHandler, refreshHandler) {
		var pfUploadWidget = widgetFromJq(uploadWidget);
		if (pfUploadWidget) {
			pfUploadWidget.onUploadError = onUploadError;
		} else {
			console.log("File upload widget not found");
		}

		$(document).on('pfAjaxComplete pfAjaxError', function(e, response) {
			if (!response) {
				return;
			}

			var respXml = response.responseXML || response.result;
			if (!respXml) {
				return;
			}

			var updates = respXml.getElementsByTagName('update');
			if (!updates) {
				return;
			}
			refreshHandler(uploadWidget, widget, addHandler);
		});

		return pfUploadWidget;
	},

	addJSFHeaders : function(pfUploadWidget, xhr) {
		xhr.setRequestHeader('X-JSF-View-State', pfUploadWidget.jq.closest(
				'form').find('input[name="javax.faces.ViewState"]').val());

		xhr.setRequestHeader('X-Auth-Token', pfUploadWidget.jq.closest('form')
				.find('input[name="token"]').val());
		xhr.setRequestHeader('Faces-Request', 'partial/ajax');
		if (pfUploadWidget.cfg.id) {
			xhr.setRequestHeader('X-JSF-Partial-Source', pfUploadWidget.cfg.id);
		}

		if (pfUploadWidget.cfg.update) {
			xhr.setRequestHeader('X-JSF-Partial-Render',
					pfUploadWidget.cfg.update);
		}

		if (pfUploadWidget.cfg.process) {
			xhr.setRequestHeader('X-JSF-Partial-Execute',
					pfUploadWidget.cfg.process);
		} else {
			xhr
					.setRequestHeader('X-JSF-Partial-Execute',
							pfUploadWidget.cfg.id);
		}

		xhr.setRequestHeader('X-JSF-Upload-Component', pfUploadWidget.cfg.id);
	},

	setupDialogAfterUpdate : function(uploadWidget, widget, addHandler) {
		var pfUploadWidget = widgetFromJq(uploadWidget);
		var fileUploadId = uploadWidget.attr('id');
		if (!pfUploadWidget) {
			console.log("Upload widget not found");
			return;
		}

		var widgetId = widget.attr('id');
		var encWidgetId = "#" + widgetId.replace(/:/g, '\\:');

		var xhr2supported = !(window.FormData === undefined);
		if (!xhr2supported) {
			console
					.log("Browser does not support XHR level2, fallback to multi-part encoding");
		}
		uploadWidget
				.fileupload({
					namespace : pfUploadWidget.cfg.namespace,
					sequentialUploads : true,
					singleFileUploads : true,
					maxChunkSize : 1024 * 2048,
					multipart : !xhr2supported,
					beforeSend : function(xhr, settings) {

						if (xhr2supported) {

							AgNO3FileUpload.addJSFHeaders(pfUploadWidget, xhr);

							var data = settings.data;

							if (data instanceof Blob) {
								data = settings.files[0];
							}

							if (data) {

								if (!data.continuation) {
									xhr.setRequestHeader(
											'X-Upload-Continuation', false);
									data.continuation = true;
								} else {
									xhr.setRequestHeader(
											'X-Upload-Continuation', true);
								}

								if (settings.chunkSize) {
									xhr.setRequestHeader('X-Upload-Chunk-Size',
											settings.chunkSize);
								}

								if (data.targetId)
									xhr.setRequestHeader('X-Upload-Target',
											data.targetId);

								if (data.transferId)
									xhr.setRequestHeader('X-Upload-TransferId',
											data.transferId);

								if (data.targetGrant)
									xhr.setRequestHeader(
											'X-Upload-Target-Grant',
											data.targetGrant);

								if (data.replaceFile)
									xhr.setRequestHeader(
											'X-Upload-Replace-File',
											data.replaceFile);

								xhr.setRequestHeader(
										'X-Upload-Replace-Files-Confirmed',
										!!data.replace_files_confirmed);

								if (data.conflictFileSize)
									xhr.setRequestHeader(
											'X-Upload-Conflict-Size',
											data.conflictFileSize);
							}
						}
						return true;
					},
					limitConcurrentUploads : 1,
					fileInput : pfUploadWidget.cfg.fileInput,
					paramName : fileUploadId,
					dataType : pfUploadWidget.cfg.dataType,
					uploadTemplate : pfUploadWidget.cfg.uploadTemplate,
					downloadTemplate : pfUploadWidget.cfg.downloadTemplate,
					formData : function(form) {
						return pfUploadWidget.createPostData();
					},
					always : function(e, data) {
						AgNO3FileUpload.onUploadComplete(pfUploadWidget)
					},
					done : function(e, data) {
						pfUploadWidget.uploadedFileCount += data.files.length;
						pfUploadWidget.removeFiles(data.files);

						var xml = data.result;

						if (xml.getElementsByTagName == undefined) {
							xml = xml[0];
						}

						PrimeFaces.ajax.Response.handle(xml, data.textStatus,
								data.jqXHR, null);
					},
					add : function(e, data) {
						if (data.originalFiles) {
							for (var i = 0, len = data.originalFiles.length; i < len; i++) {
								data.originalFiles[i].numFiles = data.originalFiles.length;
							}
						}
						addHandler(pfUploadWidget, e, data)
					},
					dropZone : []
				});

		$(encWidgetId).on('fileuploadprogress', '.file-upload',
				function(e, data) {
					AgNO3FileUpload.onUploadProgress(pfUploadWidget, e, data);
				});

		$(encWidgetId).on('fileuploadfail', '.file-upload', function(e, data) {
			AgNO3FileUpload.onUploadError(pfUploadWidget, e, data);
		});

		$(encWidgetId).on('fileuploadstart', '.file-upload', function(e) {
			AgNO3FileUpload.onUploadInit(widget, pfUploadWidget);
		});

		$(encWidgetId).on('fileuploadsend', '.file-upload', function(e, data) {
			AgNO3FileUpload.onUploadStart(pfUploadWidget, e, data);
		});

		$(encWidgetId).on('click.fileupload',
				'.file-upload .ui-fileupload-files .ui-fileupload-cancel',
				function(e) {
					AgNO3FileUpload.onUploadStop(pfUploadWidget, e);
				});
	},

	doAddFile : function(pfUploadWidget, widget, e, data, targetId,
			targetGrant, targetLabel, replaceFile, replaceFiles, canReplace) {

		if (!data) {
			console.log("Missing data");
			return;
		}

		var files = data.files;

		if (!targetId) {
			console.log("Could not determine target");
			return;
		}

		if (!targetGrant) {
			targetGrant = "";
		}

		files[0].targetId = targetId;
		files[0].targetGrant = targetGrant;
		files[0].targetLabel = targetLabel;
		files[0].replaceFile = replaceFile;
		files[0].canReplace = canReplace;

		data.formData = function(form) {
			var formData = pfUploadWidget.createPostData();
			formData.push({
				name : "upload_target",
				value : targetId
			}, {
				name : "upload_targetGrant",
				value : targetGrant
			}, {
				name : "upload_replaceFile",
				value : replaceFile
			}, {
				name : 'upload_targetLabel',
				value : targetLabel
			}, {
				name : 'upload_fileSize',
				value : data.files[0].size
			}, {
				name : 'upload_mimeType',
				value : data.files[0].type
			});

			if (replaceFiles === true) {
				formData.push({
					name : "upload_replaceFilesConfirmed",
					value : replaceFiles
				});
			}
			return formData;
		}

		if (!replaceFiles) {
			PrimeFaces.ajax.Request.handle({
				source : "form:checkNamingConflict",
				async : true,
				params : [ {
					name : 'targetId',
					value : targetId
				}, {
					name : 'targetGrant',
					value : targetGrant
				}, {
					name : 'filename',
					value : data.files[0].name
				} ],
				oncomplete : function(xhr, status, args) {
					if (args.conflictFileName) {
						AgNO3FileUpload.queueReplaceConfirm(widget,
								pfUploadWidget, e, data, args.conflictFileName,
								args.conflictFileSize, canReplace);
					} else {
						AgNO3FileUpload.checkAndUploadFile(pfUploadWidget.ucfg,
								e, data);
					}
				}
			});
		} else {
			AgNO3FileUpload.checkAndUploadFile(pfUploadWidget.ucfg, e, data);
		}
	},

	setupAfterUpdate : function(widget, addHandler) {
		var widgetId = widget.attr('id');
		var perms = widget.data('root-perms').split(',');
		var singleLevel = widget.data('single-level');
		var uploadWidget = widget.find(".file-upload");
		var pfFileTableWidget = widgetFromJq(widget.find(".file-table"));
		var encWidgetId = "#" + widgetId.replace(/:/g, '\\:');
		var thead = widget.find('.file-table thead');
		var rootDrop = thead.find('tr.root-drop-zone');
		var numCols = thead.find('th:visible').length - 1;
		var confirmDialogWidget = PF('confirmReplaceDialog');
		var emptyMessage = widget.find('.ui-treetable-empty-message td');
		emptyMessage.attr('colspan', numCols);

		if ($.inArray("UPLOAD", perms) || $.inArray("EDIT", perms)) {
			$.each($(document).find('.drop-target'), function(idx, dragDrop) {
				AgNO3FileUpload.initDragDrop($(dragDrop), handleFileMove);
			});
		}

		if (!!(navigator.userAgent.match(/Trident/) && !navigator.userAgent
				.match(/MSIE/))) {
			// IE f**ks up table width when rendering as table?!?
			pfFileTableWidget.jq.children('table').css('display', 'block');
		}

		if (!widget.data('requestQueue')) {
			widget.data('requestQueue', []);
		}

		AgNO3FileUpload
				.setupDialogAfterUpdate(uploadWidget, widget, addHandler);
	},

	makeMainViewAddHandler : function(widget) {
		var pfFileTableWidget = widgetFromJq(widget.find(".file-table"));
		return function(pfUploadWidget, e, data) {
			if (!data) {
				return;
			}

			var files = data.files;

			if (files.length > 1 || files[0].size === 0) {
				alert($('.message-no-folder-support').text());
				return;
			}

			var uploadDialog = PF('fileUploadDialog');
			var targetId;
			var targetGrant;
			var targetLabel;
			var container;
			var replaceFile = false;
			var replaceFiles = false;
			var canReplace = false;
			if (data.customData) {
				// drag and drop
				targetId = data.customData.upload_target;
				targetGrant = data.customData.upload_targetGrant;
				targetLabel = data.customData.upload_targetLabel;
				container = data.customData.upload_targetRow;
				canReplace = container.data('can-replace') === true;
			} else if (uploadDialog.jq.data('upload-target')) {
				// directory upload from dialog

				targetId = uploadDialog.jq.data('upload-target');

				console.log("Target from dialog: " + targetId);
				targetGrant = uploadDialog.jq.data('upload-target-grant');
				targetLabel = uploadDialog.jq.data('upload-target-label');
				container = widget.find('tr div[data-id=' + targetId + ']');

				if (container.length == 0) {
					container = pfUploadWidget.jq.closest('.file-browser');
				}
				replaceFiles = uploadDialog.jq.find(
						'.upload-target-replace input.replace-files').is(
						":checked");

				canReplace = container.data('can-replace') === true;
			} else if (uploadDialog.jq.data('upload-target-file')) {
				// file replace upload from dialog

				targetId = uploadDialog.jq.data('upload-target-file');
				console.log("Target file from dialog: " + targetId);
				targetGrant = uploadDialog.jq.data('upload-target-grant');
				targetLabel = uploadDialog.jq.data('upload-target-label');
				replaceFile = true;

				canReplace = container.data('can-replace') === true;
			} else if (pfUploadWidget.files && pfUploadWidget.files.length > 0) {
				console.log("Running upload");
				var running = pfUploadWidget.files[0];
				targetId = running.targetId;
				targetGrant = running.targetGrant;
				targetLabel = running.targetLabel;
				canReplace = running.canReplace;
			} else {
				console.log(data);
				console.log("Target unknown");
				return;
			}

			if (!replaceFile
					&& container
					&& (container.data("type") == "file" || container
							.data("type") == "share-root-file")) {
				console.log("Selected a file");
				var row = container.closest('tr');
				var prk = row.data('prk');
				var parentRow = pfFileTableWidget.jq.find('tr[data-rk=' + prk
						+ ']');

				if (parentRow.length === 0) {
					targetId = widget.data('root-id');
					targetGrant = widget.data('root-grant');
					targetLabel = widget.data('root-label');
				} else {
					var parentEntity = parentRow.find("a.file-name");
					var parentContainer = parentEntity.closest("div");
					targetId = parentContainer.attr('data-id');
					targetLabel = parentContainer.attr('data-label');
				}
			}
			AgNO3FileUpload.doAddFile(pfUploadWidget, widget, e, data,
					targetId, targetGrant, targetLabel, replaceFile,
					replaceFiles, canReplace);
		}
	},

	checkAndUploadFile : function(fileUpload, e, data) {
		PrimeFaces.ajax.Request.handle({
			source : "form:checkUpload",
			async : true,
			params : data.formData(),
			oncomplete : function(xhr, status, args) {
				if (!args.valid) {
					console.log("Check returned: not valid");
					console.log(status);
					console.log(args);
					console.log(data.formData());
					return;
				}
				console.log('Obtained transfer id ' + args.transferId)
				data.files[0].transferId = args.transferId;
				if ( args.chunkSize ) {
					console.log('Obtained chunk size ' + args.chunkSize);
					data.maxChunkSize = args.chunkSize;	
				}
				fileUpload.add(e, data);
			}
		});
	},

	showUploadDialog : function(ctx, replaceFile, fromDragDrop, useRoot,
			useSelection, directUpload) {
		var row = $(ctx).closest('tr');
		if (useSelection) {
			var pfFileTableWidget =widgetFromJq($(ctx).closest('.file-browser').find(
			".file-table"))
			row = pfFileTableWidget.jq.find(".ui-treetable-data > tr[data-rk="
					+ pfFileTableWidget.selections[0] + "]");
		}

		var entity = row.find(".file-name");
		var container = entity.closest("div");
		if (entity.length == 0) {
			entity = row.find(".single-level-file-name .file-name-display")
		}
		var targetName = '';
		var canReplace = false;

		if (useRoot) {
			container = $(ctx).closest('.file-browser');
			targetName = container.data('root-path');
		} else {
			targetName = entity.prop('title');
			canReplace = container.data('can-replace');
		}

		var dialog = PF('fileUploadDialog');

		if (!dialog.jq.is(':hidden')) {
			return;
		}

		// reset
		dialog.jq.data('upload-target', null);
		dialog.jq.data('upload-target-file', null);
		dialog.jq.data('upload-target-label', null);
		dialog.jq.find('.upload-target-file').css('display', 'none');
		dialog.jq.find('.upload-target-dir').css('display', 'none');

		dialog.jq.find('.upload-target-replace').css('display', 'none');
		dialog.jq.find('input[type=file]').prop('multiple', 'multiple');

		var fileuploadChooser = dialog.jq
				.find('.ui-fileupload .ui-fileupload-choose');
		if (!fromDragDrop) {
			fileuploadChooser.css('display', 'none');
			dialog.jq.data('upload-target-label', container.data('label'));
			dialog.jq.data('upload-target-grant', container.data('grant-id'));

			if (replaceFile) {
				dialog.jq.find('.upload-target-file').css('display', 'block');
				dialog.jq.data('upload-target-file', container.data('id'));
				dialog.jq.find('input[type=file]').removeProp('multiple');
			} else {
				dialog.jq.find('.upload-target-dir').css('display', 'block');
				if (useRoot) {
					dialog.jq.data('upload-target', $(ctx).closest(
							'.file-browser').data('root-id'));
				} else {
					dialog.jq.data('upload-target', container.data('id'));
				}
				dialog.jq.find('input[type=file]').prop('multiple', 'multiple');
			}

			if (canReplace && !replaceFile) {
				dialog.jq.find('.upload-target-replace')
						.css('display', 'block');
			}

			dialog.jq.find('.upload-target-name').text(targetName);
		}

		if (directUpload && !(window.FormData === undefined)) {
			console.log("Direct upload");
			dialog.jq.find('input[type=file]').click();
			fileuploadChooser.css('display', 'none');
		} else {
			fileuploadChooser.css('display', 'inline-block');
			dialog.show();
		}
	},

	closeUploadDialog : function(widget) {
		var uploadDialogWidget = PF('fileUploadDialog');
		var uploadDialog = uploadDialogWidget.jq;
		uploadDialog.data('upload-target', null);
		uploadDialog.data('upload-target-file', null);
		uploadDialogWidget.hide();

		if (refreshFileView) {
			refreshFileView();
		}
	},

	showConfirmDialog : function(widget, pfUploadWidget) {
		var confirmDialog = PF('confirmReplaceDialog');
		var entry = widget.data('requestQueue')[0];

		if (!entry) {
			confirmDialog.hide();
			console.log("Confirm complete");
			console.log(pfUploadWidget);
			if (pfUploadWidget.files && pfUploadWidget.files.length) {
				AgNO3FileUpload.showUploadDialog(widget, false, true);
			}
			return;
		}

		if (entry.data.files.length > 1) {
			console.log("Multiple files!!!");
		}

		var file = entry.data.files[0];

		console.log("Num files " + file.numFiles);
		if (file.numFiles > 1) {
			confirmDialog.jq.find('.replaceAll').css('display', '');
			confirmDialog.jq.find('.renameAll').css('display', '');
		} else {
			confirmDialog.jq.find('.replaceAll').css('display', 'none');
			confirmDialog.jq.find('.renameAll').css('display', 'none');
		}

		confirmDialog.hide();
		confirmDialog.jq.find('q.replace-file-name').text(
				entry.conflictFileName);

		if (entry.canReplace) {
			confirmDialog.jq.find('.replace').css('display', '');
			confirmDialog.jq.find('.no-replace').css('display', 'none');
		} else {
			confirmDialog.jq.find('.replace').css('display', 'none');
			confirmDialog.jq.find('.no-replace').css('display', '');
		}

		var replaceButton = confirmDialog.jq.find('.ui-button.replace');
		var renameButton = confirmDialog.jq.find('.ui-button.rename');
		var replaceAllButton = confirmDialog.jq.find('.ui-button.replaceAll');
		var renameAllButton = confirmDialog.jq.find('.ui-button.renameAll');
		var skipButton = confirmDialog.jq.find('.ui-button.skip');

		var disableButtons = function() {
			renameButton.off('click');
			replaceButton.off('click');
			replaceAllButton.off('click');
			renameAllButton.off('click');
			skipButton.off('click');
		}

		var doReplaceFile = function(e) {
			var oldFormdata = e.data.formData;
			e.data.formData = function(form) {
				var wrapped = [];
				if (oldFormdata) {
					wrapped = oldFormdata(form);
				}
				wrapped.push({
					name : "upload_replaceFilesConfirmed",
					value : true
				});

				wrapped.push({
					name : "upload_conflictFileSize",
					value : e.conflictFileSize
				});
				return wrapped;
			};

			e.data.files[0].replace_files_confirmed = true;
			e.data.files[0].conflict_file_size = e.conflictFileSize;

			AgNO3FileUpload.checkAndUploadFile(pfUploadWidget.ucfg, e.event,
					e.data);
		}

		replaceButton.on('click', function() {
			disableButtons();
			doReplaceFile(entry);
			widget.data('requestQueue').shift();
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		});

		replaceAllButton.on('click', function() {
			disableButtons();
			console.log(widget.data('requestQueue'));
			var e;
			while ((e = widget.data('requestQueue').shift())) {
				doReplaceFile(e);
			}
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		});

		renameButton.on('click', function() {
			disableButtons();
			AgNO3FileUpload.checkAndUploadFile(pfUploadWidget.ucfg,
					entry.event, entry.data);
			widget.data('requestQueue').shift();
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		});

		renameAllButton.on('click', function() {
			disableButtons();
			while ((e = widget.data('requestQueue').shift())) {
				AgNO3FileUpload.checkAndUploadFile(pfUploadWidget.ucfg,
						e.event, e.data);
			}
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		});

		skipButton.on('click', function() {
			disableButtons();
			widget.data('requestQueue').shift();
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		})

		confirmDialog.show();
	},

	queueReplaceConfirm : function(widget, pfUploadWidget, e, data,
			conflictFileName, conflictFileSize, canReplace) {
		var entry = {
			"data" : data,
			"event" : e,
			"conflictFileName" : conflictFileName,
			"conflictFileSize" : conflictFileSize,
			"canReplace" : canReplace,
		};

		if (widget.data('requestQueue').length === 0) {
			widget.data('requestQueue', [ entry ]);
			AgNO3FileUpload.showConfirmDialog(widget, pfUploadWidget);
		} else {
			widget.data('requestQueue').push(entry);
		}
	},

	onUploadProgress : function(widget, e, data) {
		for (var i = 0; i < data.files.length; i++) {
			var file = data.files[i];
			if (!file.row) {
				continue;
			}
			var progressbar = file.row.children('.ui-fileupload-progress')
					.find('> .ui-progressbar');
			var label = progressbar.find('> .ui-progressbar-label');
			if (!label.length) {
				progressbar
						.append('<div class="ui-progressbar-label" style="display: block"></div>');
			}

			var bitrateFormatted;

			if (data.bitrate > 8388608) {
				bitrateFormatted = (data.bitrate / 8388608).toFixed(1)
						+ " MB/s";
			} else if (data.bitrate > 8192) {
				bitrateFormatted = (data.bitrate / 8192).toFixed(1) + " kB/s";
			} else if (data.bitrate < 0) {
				bitrateFormatted = "0 b/s";
			} else {
				bitrateFormatted = data.bitrate.toFixed(1) + " b/s";
			}

			var label = progressbar.find('> .ui-progressbar-label').text(
					bitrateFormatted);
		}

	},

	onUploadInit : function(widget, localWidget) {
		window.onbeforeunload = function() {
			return $('.message-upload-navigation').text();
		};

		localWidget.jq.data('wasOpen', !localWidget.jq.closest('.ui-dialog')
				.is(':hidden'));

		if (widget.closeTimeout) {
			clearTimeout(widget.closeTimeout);
		}
		widget.find('a.new-window').css('display', 'inline');
		widget.find('.ui-button.close-upload').prop('disabled', true).addClass(
				'ui-state-disabled');
	},

	onUploadStart : function(localWidget, e, data) {
		var widget = localWidget.jq.closest('.file-browser');
		if (widget.length > 0 && widget.first().data('requestQueue')) {
			if (widget.first().data('requestQueue').length === 0) {
				AgNO3FileUpload.showUploadDialog(localWidget, false,
						data.fromDragDrop);
			} else {
				console.log("queue is not empty");
			}
		} else {
			AgNO3FileUpload.showUploadDialog(localWidget, false, false);
		}
	},

	onUploadStop : function(widget, e) {
		var $this = widget;
		var row = $(e.target).closest('.ui-fileupload-row');
		var fileData = row.data('filedata');

		console.log("Stopping upload");

		var removedFile = $this.files.splice(row.index(), 1);
		removedFile.uploadNext = null;
		removedFile.row = null;
		$this.removeFileRow(row);
		if (fileData.jqXHR && fileData.jqXHR.abort) {
			fileData.jqXHR.abort();
		} else {
			console.log("Abort not found");
		}

		if ($this.files.length === 0) {
			$this.disableButton($this.uploadButton);
			$this.disableButton($this.cancelButton);
		}

		e.preventDefault();

		// override buggy default behaviour
		e.stopPropagation();
	},

	onUploadComplete : function(widget) {
		var browser = widget.jq.closest('.file-browser');
		if (browser[0]) {
			AgNO3FileUpload.setupAfterUpdate(browser, AgNO3FileUpload
					.makeMainViewAddHandler(browser));
		}

		console.log("complete");

		widget.jq.find(".ui-fileupload-files .ui-fileupload-row").each(function(idx, row) {
			var foundRow = $(row);
			var fileData = foundRow.data('filedata');
			if (fileData._response.textStatus == 'success') {
				var removedFile = widget.files.splice(foundRow.index(), 1);
				removedFile.row = null;
				widget.removeFileRow(foundRow);
			}
		});

		widget.closeTimeout = setTimeout(
				function() {
					if (widget.jq.fileupload('active') === 0
							&& widget.files.length === 0) {
						window.onbeforeunload = null;
						widget.jq.closest('.ui-dialog').find(
								'.ui-button.close-upload').prop('disabled',
								false).removeClass('ui-state-disabled');
					}
					if (!widget.jq.data('wasOpen')
							&& widget.jq.fileupload('active') == 0
							&& widget.jq.children(".ui-fileupload-progress").length === 0) {
						AgNO3FileUpload.closeUploadDialog(widget);
					}
				}, 100);
	},

	onUploadError : function(widget, e, data) {
		var $this = widget;

		if (data.error) {
			return;
		}

		data.error = true;

		widget.closeTimeout = setTimeout(function() {
			if (widget.jq.fileupload('active') === 0
					&& widget.files.length === 0) {
				window.onbeforeunload = null;
				console.log("Reenabling buttons");
				widget.jq.closest('.ui-dialog').find('.ui-button.close-upload')
						.prop('disabled', false).removeClass(
								'ui-state-disabled');
			}
		}, 1000);

		if (data.errorThrown === 'abort') {
			console.log("Abort upload");
		} else if (!e.result) {
			console.log("onUploadError");
			console.log(e);
			console.log(data);
			$this.jq.find(".ui-fileupload-files .ui-fileupload-row").each(function(idx, row) {
				var foundRow = $(row);
				var fileData = foundRow.data('filedata');
				if (fileData._response.textStatus == 'error') {
					console.log("Row with error or without file data");
					if (fileData.jqXHR) {
						fileData.jqXHR.abort();
					}
					var removedFile = $this.files.splice(foundRow.index(), 1);
					removedFile.row = null;
					$this.removeFileRow(foundRow);
					if (!fileData.notified) {
						fileData.notified = true;
						if (fileData._progress.loaded) {
							alert($('.message-upload-fail').text());
						} else {
							alert($('.message-upload-fail-start').text());
						}
					}
				}
			});

			if ($this.files.length === 0) {
				$this.disableButton($this.uploadButton);
				$this.disableButton($this.cancelButton);
			}

		}

		if (widget.onUploadError) {
			console.log("Call error handler");
			widget.onUploadError();
		}

	},

	onDrop : function(e) {
		var container = $(e.target).closest("div.drop-target");
		if (container && container.data('dragOverTimeout')) {
			clearTimeout(container.data('dragOverTimeout'));
			container.data('dragOverTimeout', null);
		}

		setTimeout(function() {
			$(e.target).closest(".drop-target").removeClass('ui-state-active');
		}, 100);
	},

	onDragOver : function(e) {
		var container = $(e.target).closest("div.drop-target");

		if (!container) {
			return;
		}

		container.data('hover', true);
		container.addClass('ui-state-active');
		var activeFix = function() {
			if (container.data('hover')) {
				setTimeout(activeFix, 50);
			} else {
				container.removeClass('ui-state-active');
			}
		}

		setTimeout(activeFix, 50);
		if (!container.data('dragOverTimeout')) {
			container.data('dragOverTimeout', setTimeout(function() {
				container.trigger('draghold');
			}, 1000));
		}
	},

	onDragLeave : function(e) {
		var container = $(e.target);
		container.removeClass('ui-state-active');
		if (container && container.data('dragOverTimeout')) {
			clearTimeout(container.data('dragOverTimeout'));
			container.data('dragOverTimeout', null);
		}
	},

	initDragDrop : function(dragDrop, handleFileMove) {
		if (dragDrop.is('.ui-droppable') && dragDrop.is('ui-draggable')) {
			return;
		}

		if (!dragDrop.is('.no-drag')) {
			dragDrop.draggable({
				scope : 'files',
				delay : 100,
				helper : 'clone',
				cursorAt : {
					top : 5,
					left : 5
				},
				revert : 'invalid'
			});
		}
		AgNO3FileUpload.initDropTarget(dragDrop, handleFileMove);
	},

	initDropTarget : function(dragDrop, handleFileMove) {
		if (!dragDrop.is('.no-drop')) {
			dragDrop.droppable({
				scope : 'files',
				tolerance : 'intersect',
				drop : function(e, ui) {
					AgNO3FileUpload.onDrop(e);
					handleFileMove([ {
						name : 'dragId',
						value : ui.draggable.data('id')
					}, {
						name : 'dropId',
						value : dragDrop.data('id')
					}, {
						name : 'dragType',
						value : ui.draggable.data('type')
					}, {
						name : 'dropType',
						value : dragDrop.data('type')
					}, {
						name : 'dragGrantId',
						value : ui.draggable.data('grant-id')
					}, {
						name : 'dropGrantId',
						value : dragDrop.data('grant-id')
					} ]);
				},
				over : function(e, ui) {
					AgNO3FileUpload.onDragOver(e);
				},
				out : function(e, ui) {
					AgNO3FileUpload.onDragLeave(e);
				}
			});
		}
	},

	saveSelection : function(el) {
		if (el.selectionStart) {
			return el.selectionStart;
		} else if (document.selection) {
			el.focus();

			var r = document.selection.createRange();
			if (r == null) {
				return 0;
			}

			var re = el.createTextRange(), rc = re.duplicate();
			re.moveToBookmark(r.getBookmark());
			rc.setEndPoint('EndToStart', re);

			return rc.text.length;
		}
		return 0;
	},

	restoreSelection : function(range) {
		console.log("Restore selection " + range);
		if (range) {
			if (window.getSelection) {
				sel = window.getSelection();
				sel.removeAllRanges();
				sel.addRange(range);
			} else if (document.selection && range.select) {
				range.select();
			}
		}
	},

	onFilterChange : function(elem, event) {
		if (event.keyCode == 13) {
			AgNO3FileUpload.doUpdateFilter(elem);
			return;
		} else if (elem.setFilterTimeout) {
			clearTimeout(elem.setFilterTimeout);
		}

		elem.setFilterTimeout = setTimeout(function() {
			AgNO3FileUpload.doUpdateFilter(elem);
		}, 1000);
	},

	afterFilterChange : function(source) {
		var elem = $('.file-toolbar .filter-input')[0];

		if ($('body').data('filterSavedSelectionStart')) {
			elem.selectionStart = $('body').data('filterSavedSelectionStart');
		}

		if ($('body').data('filterSavedSelectionEnd')) {
			elem.selectionEnd = $('body').data('filterSavedSelectionEnd');
		}
	},

	doUpdateFilter : function(elem) {
		$('body').data('filterSavedSelectionStart', elem.selectionStart);
		$('body').data('filterSavedSelectionEnd', elem.selectionEnd);
		updateFilter();
	},

	initResume : function(widget, refresh) {

		if (!widget) {
			return;
		}

		var expectSize = parseInt(widget.jq.closest('fieldset').find(
				'input[name="total_size"]').val(), 10);
		var expectName = widget.jq.closest('fieldset').find(
				'input[name="expected_name"]').val();
		var chunkSize = parseInt(widget.jq.closest('fieldset').find(
				'input[name="chunk_size"]').val(), 10);

		console.log("Chunk size: " + chunkSize);

		var beforeSendFunc = function(xhr, settings) {
			AgNO3FileUpload.addJSFHeaders(widget, xhr);

			xhr.setRequestHeader('X-Upload-ChunkSize', chunkSize);
			xhr.setRequestHeader('X-Upload-Continuation', true);
			if (settings.files[0].chunkIdx) {
				xhr.setRequestHeader('X-Upload-ChunkIndex',
						settings.files[0].chunkIdx);
			}
		};

		$(".fake-upload")
				.fileupload(
						{
							add : function(e, data) {
								if (data && data.files[0]
										&& data.files[0].chunkIdx) {
									console
											&& console.log
											&& console.log("Got chunk "
													+ data.files[0].chunkIdx);
									return;
								}

								e.preventDefault();
								e.stopPropagation();

								if (!data || !data.files[0]) {
									console.log("No file selected");
									return false;
								}

								var type = data.files[0].type;
								var name = data.files[0].name;
								var size = data.files[0].size;

								if (name[0] == '/') {
									var lastSep = name.lastIndexOf('/');
									name = name.substring(lastSep + 1);
								} else if ((name[1] == ':' && name[2] == '\\')
										|| (name[0] == '\\' || name[1] == '\\')) {
									var lastSep = name.lastIndexOf('\\');
									name = name.substring(lastSep + 1);
								}

								var slice = data.files[0].slice
										|| data.files[0].mozSlice
										|| data.files[0].webkitSlice;
								if (!slice) {
									$('.message-not-supported').css('display',
											'block');
									return false;
								}

								var maxSafeSize = Number.MAX_SAFE_INTEGER || 9007199254740991;

								if ((expectSize && size != expectSize
										&& size < maxSafeSize && expectSize < maxSafeSize)
										|| name.toUpperCase() != expectName
												.toUpperCase()) {
									console.log(data.files[0]);
									console.log("Expected size " + expectSize);
									var fmt = $('.message-file-mismatch')
											.text();
									var msg = fmt;
									var replace = {
										'%origfile%' : expectName,
										'%origsize%' : expectSize,
										'%uploadfile%' : name,
										'%uploadsize%' : size
									};

									var escape_regex = function(s) {
										return s.replace(
												/[-\/\\^$*+?.()|[\]{}]/g,
												'\\$&');
									};

									for ( var placeholder in replace) {
										msg = msg.replace(
												escape_regex(placeholder),
												replace[placeholder]);
									}
									if (!window.confirm(msg)) {
										return false;
									}
								}
								$('.message-file-mismatch').css('display',
										'none');

								var missingChunks = widget.jq.closest(
										'fieldset').find(
										'input[name="missing_chunks"]').val()
										.split(',');

								if (data.jqXHR) {
									data.jqXHR.abort();
								}

								widget.jq.closest("form").find(".fake-upload")
										.css("display", "none");

								widget.files = [];
								widget.clear();

								if (!missingChunks.length
										|| !missingChunks.length
										|| !missingChunks[0]) {
									return false;
								}

								function compareNumbers(a, b) {
									return a - b;
								}

								missingChunks = missingChunks
										.sort(compareNumbers);
								var first = missingChunks[0];
								var last = missingChunks[missingChunks.length - 1];
								var expectLast = Math.floor(expectSize
										/ chunkSize);
								var rem = Math.floor(expectSize % chunkSize);
								if (rem == 0) {
									expectLast--;
								}
								var num = last - first;

								if (num == (missingChunks.length - 1)
										&& last == expectLast) {
									var startPos = first * chunkSize;
									console
											&& console.log
											&& console.log('Sequential @ '
													+ startPos);
									data.uploadedBytes = startPos;
									data.maxChunkSize = chunkSize;
									data.multipart = false;
									data.beforeSend = beforeSendFunc;
									widget.jq.fileupload('add', data);
									widget.upload();
									return;
								}

								console
										&& console.log
										&& console.log('Non Sequential ' + num
												+ ' length '
												+ missingChunks.length
												+ ' last ' + last
												+ ' expected ' + expectLast);

								var uploadNext = function(i) {
									if (i >= missingChunks.length) {
										console.log("Finished");
										widget.jq.closest('fieldset').find(
												'.select-file').css('display',
												'none');
										$('.close-button').prop('disabled',
												false).removeClass(
												'ui-state-disabled');
										return;
									}

									widget.clear();

									var chunkIdx = parseInt(missingChunks[i],
											10);
									var start = chunkIdx * chunkSize;
									var end = (chunkIdx + 1) * chunkSize;

									if (expectSize) {
										end = Math.min(expectSize, end);
									}

									console
											&& console.log
											&& console.log("Missing "
													+ chunkIdx + " start "
													+ start + " end " + end);

									var sl;
									if (data.files[0].slice) {
										sl = data.files[0].slice(start, end,
												type);
									} else if (data.files[0].mozSlice) {
										sl = data.files[0].mozSlice(start, end,
												type);
									} else if (data.files[0].webkitSlice) {
										sl = data.files[0].webkitSlice(start,
												end, type);
									}

									sl.chunkIdx = chunkIdx;
									sl.chunkNum = i;
									sl.chunkCount = missingChunks.length;
									sl.name = name;
									sl.autoUpload = true;

									var $this = widget;
									var sliceData = {
										files : [ sl ],
										multipart : false,
										beforeSend : beforeSendFunc
									};

									sliceData.uploadNext = function() {
										uploadNext(i + 1);
									}
									widget.jq.fileupload('add', sliceData);
									widget.upload();
								}

								$('.close-button').prop('disabled', true)
										.addClass('ui-state-disabled');
								uploadNext(0);
								return false;
							}
						});

		widget.jq.bind('fileuploadprogress', function(e, data) {
			AgNO3FileUpload.onUploadProgress(widget, e, data);
			for (var i = 0; i < data.files.length; i++) {
				var file = data.files[i];
				if (data.uploadNext && file.row && !file.setupLabel) {
					file.row.find("td:eq(1)").text(
							file.row.find("td:eq(1)").text() + " ("
									+ file.chunkNum + "/" + file.chunkCount
									+ ")");
					file.setupLabel = true;
				}
			}
		});

		widget.jq.bind('fileuploadfail', function(e, data) {
			AgNO3FileUpload.onUploadError(widget, e, data);
			widget.jq.closest('fieldset').find('.select-file').css('display',
					'block');
			widget.jq.closest("form").find(".fake-upload").css("display",
					'block');
			refresh();
		});

		widget.jq.bind('fileuploadsuccess', function(e, data) {
			if (!data.uploadNext) {
				console.log("All done");
				widget.jq.closest('fieldset').find('.select-file').css(
						'display', 'none');
				$('.close-button').prop('disabled', false).removeClass(
						'ui-state-disabled');
				refresh();
			} else {
				console.log(data);
			}
		});

		widget.jq.bind('fileuploadalways', function(e, data) {
			if (data.uploadNext) {
				data.uploadNext();
			} else {
				$('.close-button').prop('disabled', false).removeClass(
						'ui-state-disabled');
			}
		});

		widget.jq.on('click.fileupload',
				'.ui-fileupload-files .ui-fileupload-cancel', function(e) {
					AgNO3FileUpload.onUploadStop(widget, e);
					refresh();
					$('.close-button').prop('disabled', false).removeClass(
							'ui-state-disabled');
					widget.jq.closest('fieldset').find('.select-file').css(
							'display', 'block');
					widget.jq.closest("form").find(".fake-upload").css(
							"display", 'block');
				});
	},

	openPreview : function(ev, winid) {
		var tgt = $(ev);
		var preview = tgt.closest('div.drop-target').data('preview');

		if (!preview) {
			return;
		}

		var row = tgt.closest('tr');
		var previewDialog = PF('previewDialog');
		var fname = tgt.closest('.single-level-file-name, .file-name').text();
		var next = previewDialog.jq.find('.ui-dialog-titlebar .next-action');
		var prev = previewDialog.jq.find('.ui-dialog-titlebar .prev-action');

		var title = previewDialog.jq.find('.preview-file-name');
		var frame = previewDialog.jq.find('iframe');
		var loading = previewDialog.jq.find('.loading');

		var rk = row.data('rk');

		var fileSelector = 'tr:has(div.drop-target[data-type=file])';

		var nextrk = row.next(fileSelector).data('rk');
		var prevrk = row.prev(fileSelector).data('rk');

		console.log("n " + nextrk);
		console.log("p " + prevrk);

		title.text(fname);

		frame.hide();
		loading.show();

		previewDialog.jq.find('.ui-dialog-titlebar .ui-dialog-titlebar-close')
				.on('click', function() {
					// make sure that the frame is cleared
					console.log('closing');
					frame.attr('src', 'javascript:void(0);');
					var p = frame.parent();
					frame.remove();
					p.append('<iframe src="about:blank"></iframe>');
				});

		previewDialog.show();

		if (!winid) {
			var fcontent = frame.contents();
			var fwinid = fcontent
					.find('input[name=javax\\.faces\\.ClientWindow]');
			winid = fwinid.val();
		}

		frame.on('load', function() {
			loading.hide();
			frame.show();
		})
		frame.attr('src', preview + (winid ? '&jfwid=' + winid : ''));

		next.off('click');
		if (typeof nextrk == 'undefined') {
			next.attr('disabled', 'true');
			next.addClass('ui-state-disabled');
		} else {
			next.on('click', function() {
				var ntgt = row.closest('tr').next(fileSelector).find(
						'div.drop-target .file-name-display');
				AgNO3FileUpload.openPreview(ntgt, winid);
			});
			next.attr('disabled', 'false');
			next.removeClass('ui-state-disabled');
		}

		prev.off('click');
		if (typeof prevrk == 'undefined') {
			prev.attr('disabled', 'true');
			prev.addClass('ui-state-disabled');
		} else {
			prev.on('click', function() {
				var ptgt = row.closest('tr').prev(fileSelector).find(
						'div.drop-target .file-name-display');
				AgNO3FileUpload.openPreview(ptgt, winid);
			});
			prev.attr('disabled', 'false');
			prev.removeClass('ui-state-disabled');
		}
	},
}

function copyShareLink(shrlink) {
	if (shrlink[0]) {
		shrlink[0].select();
		shrlink[0].setSelectionRange(0, shrlink[0].value.length);
		var successful = false;
		try {
			successful = document.execCommand('copy');
		} catch (err) {
			console.log(err);
			successful = false;
		}

		if (!successful) {
			shrlink.closest('div').find('.copy-failure').fadeIn(300)
					.delay(3000).fadeOut(300);
			shrlink.closest('div').find('div.copy-button').disable();

		} else {
			shrlink.closest('div').find('.copy-success').fadeIn(300)
					.delay(3000).fadeOut(300);
		}
	}
}
