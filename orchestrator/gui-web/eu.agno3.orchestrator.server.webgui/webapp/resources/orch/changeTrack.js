/*!
 * jQuery Plugin: Are-You-Sure (Dirty Form Detection)
 * https://github.com/codedance/jquery.AreYouSure/
 *
 * Copyright (c) 2012-2014, Chris Dance and PaperCut Software http://www.papercut.com/
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * Author:  chris.dance@papercut.com
 * Version: 1.9.0
 * Date:    13th August 2014
 */
(function($) {

	$.fn.areYouSure = function(options) {

		var settings = $
				.extend(
						{
							'message' : 'You have unsaved changes!',
							'dirtyClass' : 'dirty',
							'change' : null,
							'silent' : false,
							'state' : {
								'vals' : {},
								keepDirty : false,
								dirty : false
							},
							'addRemoveFieldsMarksDirty' : false,
							'fieldEvents' : 'change keyup propertychange input',
							'fieldSelector' : ":input:not(input[type=submit]):not(input[type=button])"
						}, options);

		var getValue = function($field) {
			if ($field.hasClass('ays-ignore')
					|| $field.closest('.ays-ignore')[0]
					|| $field.hasClass('aysIgnore')
					|| $field.attr('data-ays-ignore')
					|| $field.attr('name') === undefined) {
				return null;
			}

			if ($field.attr('name').indexOf("_focus") >= 0
					|| $field.attr('name').indexOf("_activeIndex") >= 0) {
				return null;
			}

			if ($field.is(':disabled')) {
				return 'ays-disabled';
			}

			var val;
			var type = $field.attr('type');
			if ($field.is('select')) {
				type = 'select';
			}

			switch (type) {
			case 'checkbox':
			case 'radio':
				val = $field.is(':checked');
				break;
			case 'select':
				val = '';
				$field.find('option').each(function(o) {
					var $option = $(this);
					if ($option.is(':selected')) {
						val += $option.val();
					}
				});
				break;
			default:
				val = $field.val();
			}

			return val;
		};

		var storeOrigValue = function($field, reset) {
			var val = getValue($field);
			var id = $field.attr('id');
			if ( !id ) {
				id = $field.attr('name');
			}

			var state = settings.state.vals;
			var exval = id != null ? state[id] : undefined;

			if (!reset && exval !== undefined) {
				$field.data('ays-orig', state[id]);
				return;
			} else if (id) {
				state[id] = val;
			}
			$field.data('ays-orig', val);
		};

		var checkForm = function(evt) {

			var isFieldDirty = function($field) {
				var origValue = $field.data('ays-orig');
				var id = $field.attr('id');
				if ( !id ) {
					id = $field.attr('name');
				}

				if (undefined === origValue) {
					origValue = settings.state.vals[id];
				}

				if (undefined === origValue) {
					return false;
				}
				
				var val = getValue($field);
				return (val != origValue);
			};

			var $form = ($(this).is('form')) ? $(this) : $(this)
					.parents('form');

			// Test on the target first as it's the most likely to be dirty
			if (isFieldDirty($(evt.target))) {
				setDirtyStatus($form, true);
				return;
			}

			$fields = $form.find(settings.fieldSelector);

			if (settings.addRemoveFieldsMarksDirty) {
				// Check if field count has changed
				var origCount = $form.data("ays-orig-field-count");
				if (origCount != $fields.length) {
					setDirtyStatus($form, true);
					return;
				}
			}

			// Brute force - check each field
			var isDirty = false;
			$fields.each(function() {
				$field = $(this);
				if (isFieldDirty($field)) {
					isDirty = true;
					return false; // break
				}
			});

			setDirtyStatus($form, isDirty);
		};

		var initForm = function($form, reset) {
			var fields = $form.find(settings.fieldSelector);
			$(fields).each(function() {
				storeOrigValue($(this), reset);
			});
			$(fields).unbind(settings.fieldEvents, checkForm);
			$(fields).bind(settings.fieldEvents, checkForm);
			$form.on(settings.fieldEvents, settings.fieldSelector, checkForm);
			$form.data("ays-orig-field-count", $(fields).length);
			setDirtyStatus($form, settings.state.dirtyStatus);
		};

		var setDirtyStatus = function($form, isDirty) {
			if (settings.state.keepDirty) {
				isDirty = true;
			}
			var changed = isDirty != $form.hasClass(settings.dirtyClass);
			$form.toggleClass(settings.dirtyClass, isDirty);
			settings.state.dirty = isDirty;

			// Fire change event if required
			if (changed) {
				if (settings.change)
					settings.change.call($form, $form);

				if (isDirty)
					$form.trigger('dirty.areYouSure', [ $form ]);
				if (!isDirty)
					$form.trigger('clean.areYouSure', [ $form ]);
				$form.trigger('change.areYouSure', [ $form ]);
			}
		};

		var rescan = function(reset) {
			var $form = $(this);
			var fields = $form.find(settings.fieldSelector);
			$(fields).each(function() {
				var $field = $(this);
				if (!$field.data('ays-orig')) {
					storeOrigValue($field, false);
					$field.bind(settings.fieldEvents, checkForm);
				}
			});
			// Check for changes while we're here
			$form.trigger('checkform.areYouSure');
		};

		var reinitialize = function() {
			settings.state.keepDirty = false;D
			initForm($(this), true);
		}

		if (!settings.silent && !window.aysUnloadSet) {
			window.aysUnloadSet = true;
			$(window).bind('beforeunload', function(ev) {
				$dirtyForms = $("form").filter('.' + settings.dirtyClass);
				if (!settings.state.keepDirty && $dirtyForms.length == 0) {
					return;
				}
				// Prevent multiple prompts - seen on Chrome and IE
				if (navigator.userAgent.toLowerCase().match(/msie|chrome/)) {
					if (window.aysHasPrompted) {
						return;
					}
					window.aysHasPrompted = true;
					window.setTimeout(function() {
						window.aysHasPrompted = false;
					}, 900);
				}
				
				
				var triggerEl = $(ev.target.activeElement);
				if ( triggerEl.is('.ui-dialog-open')) {
					console && console.log && console.log("Opening a dialog, not blocking navigation");
					return;
				}
				ev.returnValue = settings.message;
				return settings.message;
			});
		}

		return this.each(function(elem) {
			if (!$(this).is('form')) {
				return;
			}
			var $form = $(this);
			$form.bind('reset', function() {
				setDirtyStatus($form, false);
			});
			// Add a custom events
			$form.bind('keepDirty.areYouSure', function() {
				settings.state.keepDirty = true;
			});
			$form.bind('rescan.areYouSure', rescan);
			$form.bind('reinitialize.areYouSure', reinitialize);
			$form.bind('checkform.areYouSure', checkForm);
			initForm($form, false);
			$form.trigger('checkform.areYouSure');
			$form.data('ays-initialized', true);
		});
	};
})(jQuery);

$(document)
		.ready(
				function() {
					var form = $('form.config-form');
					var save = form.find('.config-toolbar .save-button');
					var cancel = form.find('.config-toolbar .cancel-button');

					if (!document.configFormState) {
						
						initDirty = form.is('.dirty-initial');
						dirtyField = form.find('input[name=dirty]');
						
						if ( dirtyField[0] && dirtyField.val() === "true" ) {
							console && console.log && console.log("Setting dirty based on saved state");
							initDirty = true;
						}
						
						document.configFormState = {
							'vals' : {},
							keepDirty : initDirty,
							dirty : initDirty
						};
					}

					var initAys = function(form) {
						console && console.log
								&& console.log('Enabling change tracking');

						var saveWidget = widgetFromJq(save);
						var cancelWidget = widgetFromJq(cancel);
						saveWidget && saveWidget.disable();
						cancelWidget && cancelWidget.disable();
						
						saveWidget && saveWidget.jq.closest('.click-container').bind('dblclick', function() {
							form.trigger("keepDirty.areYouSure");
							dirty();
							saveWidget && saveWidget.enable();
						});
						
						cancelWidget && cancelWidget.jq.closest('.click-container').bind('dblclick', function() {
							cancelWidget && cancelWidget.enable();
						});

						dirty = function() {
							form.addClass('dirty');
							form.find('input[name=dirty]').val('true');
							form.find('.config-toolbar').addClass('dirty');
							saveWidget && saveWidget.enable();
							cancelWidget && cancelWidget.enable();
						}

						clean = function() {
							form.find('input[name=dirty]').val('false');
							form.find('.config-toolbar').removeClass('dirty');
							saveWidget && saveWidget.disable();
							cancelWidget && cancelWidget.disable();
						}

						form.on('dirty.areYouSure', function() {
							console && console.log && console.log("Dirty");
							dirty();
						});
						form.on('clean.areYouSure', function() {
							console && console.log
									&& console.log("Clean again");
							clean();
						});

						form
								.on(
										'click',
										'.edit-inherited, .revert-defaults, .add-object, .remove-object, .listEditor button',
										function() {
											console
													&& console.log
													&& console
															.log("Trigger change by button");
											form
													.trigger("keepDirty.areYouSure");
											dirty();
										});

						form.on('slide', '.inputComponents .ui-slider',
								function() {
									form.trigger('checkform.areYouSure');
								});

						form.on('inner.dirty', function() {
							console && console.log
									&& console.log("Have inner dirty");
							dirty();
							form.trigger('keepDirty.areYouSure');
						});

						form
								.areYouSure({
									'silent' : false,
									'state' : document.configFormState,
									'message' : 'Unsaved chanages were detected. Leaving this page will discard them. Continue?',
									'fieldEvents' : 'change keyup propertychange input',
									'fieldSelector' : "fieldset :input:not(input[type=submit]):not(input[type=button]):not(input[type=hidden]):not(.ignoreChange), "
											+ ".inputComponents :input[type=hidden]:not(.ignoreChange)"
								});
					}

					$(document)
							.on(
									'pfAjaxComplete pfAjaxError',
									function(e, response) {
										if (!response) {
											return;
										}

										var respXml = response.responseXML
												|| response.result;
										if (!respXml) {
											return;
										}

										var updates = respXml
												.getElementsByTagName('update');
										if (!updates) {
											return;
										}

										var form = $('form.config-form');
										if (!form.data('ays-initialized')) {
											console
													.log("Reinitialize after AJAX update");
											initAys(form);
										} else {
											console
													.log("Rescan after AJAX update");
											form.trigger('rescan.areYouSure');
										}

										if (form.is('.dirty')) {
											form.trigger("dirty.areYouSure");
										} else {
											form.trigger("clean.areYouSure");
										}
									});

					initAys(form);
				});

function dirtyReturn(that, args) {
	if (!args || !args.dirty) {
		console.log("Return not dirty");
		console.log(args);
		return;
	}
	console && console.log
			&& console.log("Have dirty return from inner editor");
	$(document.getElementById(that.source)).trigger('inner.dirty');
}

function savedConfig(saved) {
	if (!saved) {
		return;
	}
	var form = $('form.config-form');
	document.configFormState.vals = {};
	document.configFormState.keepDirty = false;
	document.configFormState.dirty = false;
	form.trigger("reinitialize.areYouSure");
}
