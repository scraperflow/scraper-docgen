document.addEventListener('DOMContentLoaded', function() {
    if(window.location.hash) {
        showDoc(window.location.hash.substr(1));
    }

}, false);

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
                link.href = '#' + node['extends'][i];
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

    let fields = document.createElement('h4');
    fields.innerHTML = "Configuration";
    document.getElementById('main').appendChild(fields);


    let fieldsDiv = document.createElement('div');
    fieldsDiv.id = 'fields';
    document.getElementById('main').appendChild(fieldsDiv);
    addFields(node);

    window.location.hash = e;
}

function addFields(node) {
    for (let i = 0; i < node['fields'].length; i++) {
        let field = document.createElement('div');
        field.classList.add('field');

        {
            let name = document.createElement('span');
            name.innerHTML = node['fields'][i]['name'];
            name.classList.add('fieldName');
            field.appendChild(name);
        }
        {
            let defaultVal = document.createElement('span');
            if(node['fields'][i]['defaultValue'] !== 'null') {
                defaultVal.innerHTML = node['fields'][i]['defaultValue'];
                defaultVal.classList.add('defaultValue');
                field.appendChild(defaultVal);
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
    }

}