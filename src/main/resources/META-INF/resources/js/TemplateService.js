export class TemplateService {
    constructor(url) {
        this.imageDownloadBaseUrl = url;
    }

    render(name, data) {
        var html = "";

        html = html + this.header() + "\n";

        this.folders = false;
        data.items.forEach(item => {html = html + this.element(item)});

        html = html + this.footer() + "\n";

        if (! this.folders) {
            console.log("TemplateService.render: turning images into gallery");
        }

        return html;
    }

    header() {


        return `
    <bread-crumbs></bread-crumbs>
    <ul class="auto-grid">
        `;
    }

    footer() {
        return `    </ul>
`;
    }

    element(item) {
        if ("folder" === item.type) {
            return this.renderFolder(item);
        }

        return this.renderPicture(item);
    }

    renderPicture(item) {
        const imageUrl = this.imageDownloadBaseUrl + item.path;

        return `
        <li>
            <img src="${imageUrl}" alt="${item.title}" width="90%">
        </li>
`;
    }

    renderFolder(item) {
        this.folders = true;

        return `
        <li>
            <a href="#${item.path}">
                <img src="/img/folder.png" width="90%" height="90%" alt="${item.title}">
            </a>
            <span>${item.title}</span>
        </li>
`;
    }
}
