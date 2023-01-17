TW.IDE.Widgets.plugin_name = function () {

	this.widgetIconUrl = function() {
		return  "'../Common/extensions/project_name/ui/plugin_name/default_widget_icon.ide.png'";
	};

	this.widgetProperties = function () {
		return {
			'name': 'plugin_name',
			'description': '',
			'category': ['Common'],
			'properties': {
				'plugin_name Property': {
					'baseType': 'STRING',
					'defaultValue': 'plugin_name Property default value',
					'isBindingTarget': true
				}
			}
		}
	};

	this.afterSetProperty = function (name, value) {
		var thisWidget = this;
		var refreshHtml = false;
		switch (name) {
			case 'Style':
			case 'plugin_name Property':
				thisWidget.jqElement.find('.plugin_name-property').text(value);
			case 'Alignment':
				refreshHtml = true;
				break;
			default:
				break;
		}
		return refreshHtml;
	};

	this.renderHtml = function () {
		// return any HTML you want rendered for your widget
		// If you want it to change depending on properties that the user
		// has set, you can use this.getProperty(propertyName).
		return 	'<div class="widget-content widget-plugin_name">' +
					'<span class="plugin_name-property">' + this.getProperty('plugin_name Property') + '</span>' +
				'</div>';
	};

	this.afterRender = function () {
		// NOTE: this.jqElement is the jquery reference to your html dom element
		// 		 that was returned in renderHtml()

		// get a reference to the value element
		valueElem = this.jqElement.find('.plugin_name-property');
		// update that DOM element based on the property value that the user set
		// in the mashup builder
		valueElem.text(this.getProperty('plugin_name Property'));
	};

};