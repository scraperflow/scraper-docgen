document.addEventListener('DOMContentLoaded', function() {
    if(window.location.hash) {
        showDoc(window.location.hash.substr(1));
    }

    showInitial()

    const selectElement = document.querySelector('input');
    selectElement.addEventListener('input', function (evt) {
        filterNodesByText(this.value);
    });

    filterNodesByText(selectElement.value)

}, false);

showFilter = {lambda: true, io: true, stateful: true, flow: true, stream: true}

window.onhashchange = function change(h) {
    showDoc(window.location.hash.substr(1));
};

function showDoc(e) {
    // noinspection JSUnresolvedVariable
    let node = map[e];

    document.getElementById('main').innerHTML = '';

    let extendedNodes = document.createElement('span');
    {
        for (let i = 0; i < node['extends'].length; i++) {
            {
                let link = document.createElement('a');
                nodename = node['extends'][i];
                // remove Node from link
                if (nodename === "Node" || nodename === "StreamNode" || nodename === "FunctionalNode") { link.href = '#' + node['extends'][i] }
                else { link.href = '#' + nodename.slice(0, -4) }

                link.innerText = node['extends'][i];
                extendedNodes.appendChild(link);
                let space = document.createElement('span');
                space.innerText = " > ";
                extendedNodes.appendChild(space);
            }
        }
    }

    let h = document.createElement('h3');
    let hspan = document.createElement('span');
    hspan.innerHTML = ' v' + node['version'];
    hspan.classList.add('version');
    h.innerHTML = extendedNodes.outerHTML + e + hspan.outerHTML;

    document.getElementById('main').appendChild(h);

    let doc = document.createElement('span');
    doc.innerHTML = node["doc"]["txt"];
    document.getElementById('main').appendChild(doc);



    document.getElementById('main').appendChild(document.createElement('hr'));
    let template = document.createElement('h4');
    template.innerHTML = "Template";
    let templatePre = document.createElement('pre');
    if(e !== "Node" && e !== "FunctionalNode" && e !== "StreamNode") templatePre.innerText = "type: " + e + "\n";
    template.appendChild(templatePre);
    document.getElementById('main').appendChild(template);


    document.getElementById('main').appendChild(document.createElement('hr'));
    let fields = document.createElement('h4');
    fields.innerHTML = "Configuration";
    document.getElementById('main').appendChild(fields);


    let fieldsDiv = document.createElement('div');
    fieldsDiv.id = 'fields';
    document.getElementById('main').appendChild(fieldsDiv);


    addFields(node, templatePre);

    window.location.hash = e;
}

function addFields(node, templatePre) {


    for (let i = 0; i < node['fields'].length; i++) {
        let isMandatory = false;
        let defaultValue = null;
        let fieldName;


        let field = document.createElement('div');
        field.classList.add('field');

        {
            let name = document.createElement('span');
            name.innerHTML = node['fields'][i]['name'];
            name.classList.add('fieldName');
            field.appendChild(name);
            fieldName = node['fields'][i]['name'];
        }
        {
            let defaultVal = document.createElement('span');
            if(node['fields'][i]['defaultValue'] !== 'null') {
                defaultVal.innerHTML = node['fields'][i]['defaultValue'];
                defaultVal.classList.add('defaultValue');
                field.appendChild(defaultVal);
                defaultValue = node['fields'][i]['defaultValue'];
            }

        }
        {
            let type = document.createElement('span');
            type.innerText = node['fields'][i]['type'];
            type.classList.add('version');
            field.appendChild(type);
        }
        {

            let doc = document.createElement('div');
            doc.classList.add('tags');

            {
                let tag = document.createElement('span');
                tag.classList.add('tag');
                let mandatory = node['fields'][i]['mandatory'];
                if(mandatory === 'true') {
                    tag.classList.add('mandatory');
                    tag.innerHTML = 'mandatory';
                    isMandatory = true;
                } else {
                    tag.classList.add('optional');
                    tag.innerHTML = 'optional';
                }
                doc.appendChild(tag);
            }

            {
                let tag = document.createElement('span');
                tag.classList.add('tag');
                let argument = node['fields'][i]['argument'];
                if(argument === 'true') {
                    tag.classList.add('argument');
                    tag.innerHTML = 'argument';
                }
                doc.appendChild(tag);
            }



            // doc.innerText = node['fields'][i]['txt'];
            field.appendChild(doc);
        }
        {
            let doc = document.createElement('div');
            doc.classList = ['fieldDoc'];
            doc.innerHTML = node['fields'][i]['txt'];
            field.appendChild(doc);
        }

        document.getElementById('fields').appendChild(field);


        // create template pre text
        if(fieldName !== "type") {
            templatePre.innerText = templatePre.innerText
                + (isMandatory?"":"#")
                + fieldName +": "
                + (defaultValue !== null ? defaultValue : "")
                + "\n"
            ;
        }

    }

}

function filterNodes(category) {
    showFilter[category] = !showFilter[category];

    let nodes = document.getElementsByClassName('node-list');
    for (let i = 0; i < nodes.length; i++) {
        filterNode(category, nodes[i])
    }

    let button = document.getElementsByClassName('sidefilter')[0].getElementsByClassName(category)[0];
    if(showFilter[category]) { button.classList.remove("hide-btn") }
    else { button.classList.add("hide-btn") }
}

function showInitial() {
    let cats = ["lambda", "stream", "stateful", "flow", "io"]
    for (let i = 0; i < cats.length; i++) {
        let cat = cats[i];
        let nodes = document.getElementsByClassName(cat);
        for (let j = 0; j < nodes.length; j++) {
            let node = nodes[j];
            node.parentNode.classList.add("show-initial-"+cat)
        }
    }
}

function filterNode(category, node) {
    node.classList.remove("show-initial-"+category)

    let matching = node.getElementsByClassName(category).length > 0;
    if (matching) {
        if(showFilter[category]) {
            node.classList.remove("hide-"+category)
            node.classList.add("show-"+category)
        }
        else {
            node.classList.add("hide-"+category)
            node.classList.remove("show-"+category)
        }
    }
}

function filterNodesByText(name) {
    let nodes = document.getElementsByClassName('node-list');
    for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]
        let els = node.querySelectorAll("a");

        if(!els[0].text.toLowerCase().includes(name.toLowerCase())) {
            node.classList.add("hide")
        } else {
            node.classList.remove("hide")
        }
    }
}
