import {FullCalendar} from '@vaadin/flow-frontend/full-calendar.js';
import tippy from 'tippy.js';

export class FullCalendarWithTooltip extends FullCalendar {
    static get is() {
        return 'full-calendar-with-tooltip';
    }

    _initCalendar() {
        super._initCalendar();
        this.getCalendar().setOption("eventDidMount", e => {
            this.initTooltip(e);
        });
    }

    initTooltip(e) {
        if (!e.isMirror) {
            e.el.addEventListener("mouseenter", () => {
                let tooltip = e.event.getCustomProperty("tooltip");

                if(tooltip) {
                    e.el._tippy = tippy(e.el, {
                        theme: 'light',
                        content: tooltip,
                        trigger: 'manual'
                    });

                    e.el._tippy.show();
                }
            })

            e.el.addEventListener("mouseleave", () => {
                if (e.el._tippy) {
                    e.el._tippy.destroy();
                }
            })

        }
    }
}

customElements.define(FullCalendarWithTooltip.is, FullCalendarWithTooltip);
